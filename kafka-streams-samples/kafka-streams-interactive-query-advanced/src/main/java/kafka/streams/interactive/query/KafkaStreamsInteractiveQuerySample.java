/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.streams.interactive.query;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import kafka.streams.interactive.query.avro.PlayEvent;
import kafka.streams.interactive.query.avro.Song;
import kafka.streams.interactive.query.avro.SongPlayCount;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class KafkaStreamsInteractiveQuerySample {
	static final String PLAY_EVENTS = "play-events";
	static final String SONG_FEED = "song-feed";
	static final String TOP_FIVE_KEY = "all";
	static final String TOP_FIVE_SONGS_STORE = "top-five-songs";
	static final String ALL_SONGS = "all-songs";

	@Autowired
	private InteractiveQueryService interactiveQueryService;

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsInteractiveQuerySample.class, args);
	}

	public static class KStreamMusicSampleApplication {

		private static final Long MIN_CHARTABLE_DURATION = 30 * 1000L;
		private static final String SONG_PLAY_COUNT_STORE = "song-play-count";

		static final String TOP_FIVE_SONGS_BY_GENRE_STORE = "top-five-songs-by-genre";
		static final String TOP_FIVE_KEY = "all";

		@Bean
		public BiConsumer<KStream<String, PlayEvent>, KTable<Long, Song>> process() {

			return (s, t) -> {
				// create and configure the SpecificAvroSerdes required in this example
				final Map<String, String> serdeConfig = Collections.singletonMap(
						AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

				final SpecificAvroSerde<PlayEvent> playEventSerde = new SpecificAvroSerde<>();
				playEventSerde.configure(serdeConfig, false);

				final SpecificAvroSerde<Song> keySongSerde = new SpecificAvroSerde<>();
				keySongSerde.configure(serdeConfig, true);

				final SpecificAvroSerde<Song> valueSongSerde = new SpecificAvroSerde<>();
				valueSongSerde.configure(serdeConfig, false);

				final SpecificAvroSerde<SongPlayCount> songPlayCountSerde = new SpecificAvroSerde<>();
				songPlayCountSerde.configure(serdeConfig, false);

				// Accept play events that have a duration >= the minimum
				final KStream<Long, PlayEvent> playsBySongId =
						s.filter((region, event) -> event.getDuration() >= MIN_CHARTABLE_DURATION)
								// repartition based on song id
								.map((key, value) -> KeyValue.pair(value.getSongId(), value));

				// join the plays with song as we will use it later for charting
				final KStream<Long, Song> songPlays = playsBySongId.leftJoin(t,
						(value1, song) -> song,
						Joined.with(Serdes.Long(), playEventSerde, valueSongSerde));

				// create a state store to track song play counts
				final KTable<Song, Long> songPlayCounts = songPlays.groupBy((songId, song) -> song,
						Serialized.with(keySongSerde, valueSongSerde))
						.count(Materialized.<Song, Long, KeyValueStore<Bytes, byte[]>>as(SONG_PLAY_COUNT_STORE)
								.withKeySerde(valueSongSerde)
								.withValueSerde(Serdes.Long()));

				final TopFiveSerde topFiveSerde = new TopFiveSerde();

				// Compute the top five charts for each genre. The results of this computation will continuously update the state
				// store "top-five-songs-by-genre", and this state store can then be queried interactively via a REST API (cf.
				// MusicPlaysRestService) for the latest charts per genre.
				songPlayCounts.groupBy((song, plays) ->
								KeyValue.pair(song.getGenre().toLowerCase(),
										new SongPlayCount(song.getId(), plays)),
						Serialized.with(Serdes.String(), songPlayCountSerde))
						// aggregate into a TopFiveSongs instance that will keep track
						// of the current top five for each genre. The data will be available in the
						// top-five-songs-genre store
						.aggregate(TopFiveSongs::new,
								(aggKey, value, aggregate) -> {
									aggregate.add(value);
									return aggregate;
								},
								(aggKey, value, aggregate) -> {
									aggregate.remove(value);
									return aggregate;
								},
								Materialized.<String, TopFiveSongs, KeyValueStore<Bytes, byte[]>>as(TOP_FIVE_SONGS_BY_GENRE_STORE)
										.withKeySerde(Serdes.String())
										.withValueSerde(topFiveSerde)
						);

				// Compute the top five chart. The results of this computation will continuously update the state
				// store "top-five-songs", and this state store can then be queried interactively via a REST API (cf.
				// MusicPlaysRestService) for the latest charts per genre.
				songPlayCounts.groupBy((song, plays) ->
								KeyValue.pair(TOP_FIVE_KEY,
										new SongPlayCount(song.getId(), plays)),
						Serialized.with(Serdes.String(), songPlayCountSerde))
						.aggregate(TopFiveSongs::new,
								(aggKey, value, aggregate) -> {
									aggregate.add(value);
									return aggregate;
								},
								(aggKey, value, aggregate) -> {
									aggregate.remove(value);
									return aggregate;
								},
								Materialized.<String, TopFiveSongs, KeyValueStore<Bytes, byte[]>>as(TOP_FIVE_SONGS_STORE)
										.withKeySerde(Serdes.String())
										.withValueSerde(topFiveSerde)
						);
			};

		}
	}

	@RestController
	public class FooController {

		private final Log logger = LogFactory.getLog(getClass());

		@RequestMapping("/song/idx")
		public SongBean song(@RequestParam(value="id") Long id) {
			final ReadOnlyKeyValueStore<Long, Song> songStore =
					interactiveQueryService.getQueryableStore(KafkaStreamsInteractiveQuerySample.ALL_SONGS, QueryableStoreTypes.<Long, Song>keyValueStore());

			final Song song = songStore.get(id);
			if (song == null) {
				throw new IllegalArgumentException("hi");
			}
			return new SongBean(song.getArtist(), song.getAlbum(), song.getName());
		}

		@RequestMapping("/charts/top-five")
		@SuppressWarnings("unchecked")
		public List<SongPlayCountBean> topFive(@RequestParam(value="genre") String genre) {

			HostInfo hostInfo = interactiveQueryService.getHostInfo(KafkaStreamsInteractiveQuerySample.TOP_FIVE_SONGS_STORE,
					KafkaStreamsInteractiveQuerySample.TOP_FIVE_KEY, new StringSerializer());

			if (interactiveQueryService.getCurrentHostInfo().equals(hostInfo)) {
				logger.info("Top Five songs request served from same host: " + hostInfo);
				return topFiveSongs(KafkaStreamsInteractiveQuerySample.TOP_FIVE_KEY, KafkaStreamsInteractiveQuerySample.TOP_FIVE_SONGS_STORE);
			}
			else {
				//find the store from the proper instance.
				logger.info("Top Five songs request served from different host: " + hostInfo);
				RestTemplate restTemplate = new RestTemplate();
				return restTemplate.postForObject(
						String.format("http://%s:%d/%s", hostInfo.host(),
								hostInfo.port(), "charts/top-five?genre=Punk"), "punk", List.class);
			}
		}

		private List<SongPlayCountBean> topFiveSongs(final String key,
													 final String storeName) {
			final ReadOnlyKeyValueStore<String, TopFiveSongs> topFiveStore =
					interactiveQueryService.getQueryableStore(storeName, QueryableStoreTypes.<String, TopFiveSongs>keyValueStore());

			// Get the value from the store
			final TopFiveSongs value = topFiveStore.get(key);
			if (value == null) {
				throw new IllegalArgumentException(String.format("Unable to find value in %s for key %s", storeName, key));
			}
			final List<SongPlayCountBean> results = new ArrayList<>();
			value.forEach(songPlayCount -> {

				HostInfo hostInfo = interactiveQueryService.getHostInfo(KafkaStreamsInteractiveQuerySample.ALL_SONGS,
						songPlayCount.getSongId(), new LongSerializer());

				if (interactiveQueryService.getCurrentHostInfo().equals(hostInfo)) {
					logger.info("Song info request served from same host: " + hostInfo);

					final ReadOnlyKeyValueStore<Long, Song> songStore =
							interactiveQueryService.getQueryableStore(KafkaStreamsInteractiveQuerySample.ALL_SONGS, QueryableStoreTypes.<Long, Song>keyValueStore());

					final Song song = songStore.get(songPlayCount.getSongId());
					results.add(new SongPlayCountBean(song.getArtist(),song.getAlbum(), song.getName(),
							songPlayCount.getPlays()));
				}
				else {
					logger.info("Song info request served from different host: " + hostInfo);
					RestTemplate restTemplate = new RestTemplate();
					SongBean song = restTemplate.postForObject(
							String.format("http://%s:%d/%s", hostInfo.host(),
									hostInfo.port(), "song/idx?id=" + songPlayCount.getSongId()),  "id", SongBean.class);
					results.add(new SongPlayCountBean(song.getArtist(),song.getAlbum(), song.getName(),
							songPlayCount.getPlays()));
				}


			});
			return results;
		}
	}

	/**
	 * Serde for TopFiveSongs
	 */
	private static class TopFiveSerde implements Serde<TopFiveSongs> {

		@Override
		public Serializer<TopFiveSongs> serializer() {

			return new Serializer<TopFiveSongs>() {
				@Override
				public void configure(final Map<String, ?> map, final boolean b) {
				}

				@Override
				public byte[] serialize(final String s, final TopFiveSongs topFiveSongs) {

					final ByteArrayOutputStream out = new ByteArrayOutputStream();
					final DataOutputStream
							dataOutputStream =
							new DataOutputStream(out);
					try {
						for (SongPlayCount songPlayCount : topFiveSongs) {
							dataOutputStream.writeLong(songPlayCount.getSongId());
							dataOutputStream.writeLong(songPlayCount.getPlays());
						}
						dataOutputStream.flush();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					return out.toByteArray();
				}
			};
		}

		@Override
		public Deserializer<TopFiveSongs> deserializer() {

			return (s, bytes) -> {
				if (bytes == null || bytes.length == 0) {
					return null;
				}
				final TopFiveSongs result = new TopFiveSongs();

				final DataInputStream
						dataInputStream =
						new DataInputStream(new ByteArrayInputStream(bytes));

				try {
					while(dataInputStream.available() > 0) {
						result.add(new SongPlayCount(dataInputStream.readLong(),
								dataInputStream.readLong()));
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return result;
			};
		}
	}

	/**
	 * Used in aggregations to keep track of the Top five songs
	 */
	static class TopFiveSongs implements Iterable<SongPlayCount> {
		private final Map<Long, SongPlayCount> currentSongs = new HashMap<>();
		private final TreeSet<SongPlayCount> topFive = new TreeSet<>((o1, o2) -> {
			final int result = o2.getPlays().compareTo(o1.getPlays());
			if (result != 0) {
				return result;
			}
			return o1.getSongId().compareTo(o2.getSongId());
		});

		public void add(final SongPlayCount songPlayCount) {
			if(currentSongs.containsKey(songPlayCount.getSongId())) {
				topFive.remove(currentSongs.remove(songPlayCount.getSongId()));
			}
			topFive.add(songPlayCount);
			currentSongs.put(songPlayCount.getSongId(), songPlayCount);
			if (topFive.size() > 5) {
				final SongPlayCount last = topFive.last();
				currentSongs.remove(last.getSongId());
				topFive.remove(last);
			}
		}

		void remove(final SongPlayCount value) {
			topFive.remove(value);
			currentSongs.remove(value.getSongId());
		}


		@Override
		public Iterator<SongPlayCount> iterator() {
			return topFive.iterator();
		}
	}

}
