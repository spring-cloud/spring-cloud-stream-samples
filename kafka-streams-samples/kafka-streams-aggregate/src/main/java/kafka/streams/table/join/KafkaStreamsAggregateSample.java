/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.streams.table.join;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.QueryableStoreRegistry;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class KafkaStreamsAggregateSample {

	@Autowired
	private QueryableStoreRegistry queryableStoreRegistry;

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsAggregateSample.class, args);
	}

	@EnableBinding(KafkaStreamsProcessorX.class)
	@EnableAutoConfiguration
	public static class KafkaStreamsAggregateSampleApplication {

		@StreamListener("input")
		public void process(KStream<Object, DomainEvent> input) {
			ObjectMapper mapper = new ObjectMapper();
			Serde<DomainEvent> domainEventSerde = new JsonSerde<>( DomainEvent.class, mapper );

			input
					.groupBy(
							(s, domainEvent) -> domainEvent.boardUuid,
							Serialized.with(null, domainEventSerde))
					.aggregate(
							String::new,
							(s, domainEvent, board) -> board.concat(domainEvent.eventType),
							Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("test-events-snapshots").withKeySerde(Serdes.String()).
									withValueSerde(Serdes.String())
					);
		}
	}

	@RestController
	public class FooController {

		@RequestMapping("/events")
		public String events() {

			final ReadOnlyKeyValueStore<String, String> topFiveStore =
					queryableStoreRegistry.getQueryableStoreType("test-events-snapshots", QueryableStoreTypes.<String, String>keyValueStore());
			return topFiveStore.get("12345");
		}
	}

	interface KafkaStreamsProcessorX {

		@Input("input")
		KStream<?, ?> input();
	}
}
