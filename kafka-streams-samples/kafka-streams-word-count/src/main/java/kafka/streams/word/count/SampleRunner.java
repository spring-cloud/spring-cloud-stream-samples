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

package kafka.streams.word.count;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides a test source and sink to trigger the kafka streams processor
 * and test the output respectively.
 *
 * @author Soby Chacko
 */
public class SampleRunner {

	//Following code is only used as a test harness.

	//Following source is used as test producer.
	@EnableBinding(TestSource.class)
	static class TestProducer {

		private AtomicBoolean semaphore = new AtomicBoolean(true);

		private String[] randomWords = new String[]{"foo", "bar", "foobar", "baz", "fox"};
		private Random random = new Random();

		@Bean
		@InboundChannelAdapter(channel = TestSource.OUTPUT, poller = @Poller(fixedDelay = "1000"))
		public MessageSource<String> sendTestData() {
			return () -> {
				int idx = random.nextInt(5);
				return new GenericMessage<>(randomWords[idx]);
			};
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
}
