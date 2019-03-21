/*
 * Copyright 2015 the original author or authors.
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

package multibinder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.ForeachAction;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.core.MessageSource;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Marius Bogoevici
 * @author Soby Chacko
 */
@EnableBinding(Processor.class)
public class BridgeTransformer {

	@Autowired
	private InteractiveQueryService interactiveQueryService;

	@StreamListener(Processor.INPUT)
	@SendTo(Processor.OUTPUT)
	public Object transform(Object payload) {
		return payload;
	}

	//Following source is used as test producer.
	@EnableBinding(TestSource.class)
	static class TestProducer {

		private AtomicBoolean semaphore = new AtomicBoolean(true);

		@Bean
		@InboundChannelAdapter(channel = TestSource.OUTPUT, poller = @Poller(fixedDelay = "1000"))
		public MessageSource<String> sendTestData() {
			return () ->
					new GenericMessage<>(this.semaphore.getAndSet(!this.semaphore.get()) ? "foo" : "bar");

		}
	}

	//Following sink is used as test consumer for the above processor. It logs the data received through the processor.
	@EnableBinding(TestSink.class)
	static class TestConsumer {

		private final Log logger = LogFactory.getLog(getClass());

		@StreamListener(TestSink.INPUT)
		public void receive(String data) {
			logger.info("Data received..." + data);
		}
	}

	@EnableBinding(KafkaStreamsProcessorX.class)
	static class KafkaStreamsAggregateSampleApplication {

		@StreamListener("input2")
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
					interactiveQueryService.getQueryableStore("test-events-snapshots", QueryableStoreTypes.<String, String>keyValueStore());
			return topFiveStore.get("12345");
		}
	}

	interface TestSink {

		String INPUT = "input1";

		@Input(INPUT)
		SubscribableChannel input1();

	}

	interface TestSource {

		String OUTPUT = "output1";

		@Output(TestSource.OUTPUT)
		MessageChannel output();

	}

	interface KafkaStreamsProcessorX {

		@Input("input2")
		KStream<?, ?> input2();
	}

}
