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

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import kafka.streams.interactive.query.avro.PlayEvent;
import kafka.streams.interactive.query.avro.Song;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.*;

/**
 * @author Soby Chacko
 */
public class Producers {

	public static void main(String... args) throws Exception {

		final Map<String, String> serdeConfig = Collections.singletonMap(
				AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
		final SpecificAvroSerializer<PlayEvent> playEventSerializer = new SpecificAvroSerializer<>();
		playEventSerializer.configure(serdeConfig, false);
		final SpecificAvroSerializer<Song> songSerializer = new SpecificAvroSerializer<>();
		songSerializer.configure(serdeConfig, false);

		final List<Song> songs = Arrays.asList(new Song(1L,
						"Fresh Fruit For Rotting Vegetables",
						"Dead Kennedys",
						"Chemical Warfare",
						"Punk"),
				new Song(2L,
						"We Are the League",
						"Anti-Nowhere League",
						"Animal",
						"Punk"),
				new Song(3L,
						"Live In A Dive",
						"Subhumans",
						"All Gone Dead",
						"Punk"),
				new Song(4L,
						"PSI",
						"Wheres The Pope?",
						"Fear Of God",
						"Punk"),
				new Song(5L,
						"Totally Exploited",
						"The Exploited",
						"Punks Not Dead",
						"Punk"),
				new Song(6L,
						"The Audacity Of Hype",
						"Jello Biafra And The Guantanamo School Of "
								+ "Medicine",
						"Three Strikes",
						"Punk"),
				new Song(7L,
						"Licensed to Ill",
						"The Beastie Boys",
						"Fight For Your Right",
						"Hip Hop"),
				new Song(8L,
						"De La Soul Is Dead",
						"De La Soul",
						"Oodles Of O's",
						"Hip Hop"),
				new Song(9L,
						"Straight Outta Compton",
						"N.W.A",
						"Gangsta Gangsta",
						"Hip Hop"),
				new Song(10L,
						"Fear Of A Black Planet",
						"Public Enemy",
						"911 Is A Joke",
						"Hip Hop"),
				new Song(11L,
						"Curtain Call - The Hits",
						"Eminem",
						"Fack",
						"Hip Hop"),
				new Song(12L,
						"The Calling",
						"Hilltop Hoods",
						"The Calling",
						"Hip Hop")

		);

		Map<String, Object> props = new HashMap<>();
		props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, playEventSerializer.getClass());


		Map<String, Object> props1 = new HashMap<>(props);
		props1.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
		props1.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, songSerializer.getClass());

		DefaultKafkaProducerFactory<Long, Song> pf1 = new DefaultKafkaProducerFactory<>(props1);
		KafkaTemplate<Long, Song> template1 = new KafkaTemplate<>(pf1, true);
		template1.setDefaultTopic(KafkaStreamsInteractiveQuerySample.SONG_FEED);

		songs.forEach(song -> {
			System.out.println("Writing song information for '" + song.getName() + "' to input topic " +
					KafkaStreamsInteractiveQuerySample.SONG_FEED);
			template1.sendDefault(song.getId(), song);
		});


		DefaultKafkaProducerFactory<String, PlayEvent> pf = new DefaultKafkaProducerFactory<>(props);
		KafkaTemplate<String, PlayEvent> template = new KafkaTemplate<>(pf, true);
		template.setDefaultTopic(KafkaStreamsInteractiveQuerySample.PLAY_EVENTS);


		final long duration = 60 * 1000L;
		final Random random = new Random();

		// send a play event every 100 milliseconds
		while (true) {
			final Song song = songs.get(random.nextInt(songs.size()));
			System.out.println("Writing play event for song " + song.getName() + " to input topic " +
					KafkaStreamsInteractiveQuerySample.PLAY_EVENTS);
			template.sendDefault("uk", new PlayEvent(song.getId(), duration));

			Thread.sleep(100L);
		}
	}

}
