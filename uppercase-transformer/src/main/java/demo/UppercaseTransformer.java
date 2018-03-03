/*
 * Copyright 2015 the original author or authors.
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

package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Dave Syer
 * @author Soby Chacko
 */
@EnableBinding(Processor.class)
public class UppercaseTransformer {

	private static Logger logger = LoggerFactory.getLogger(UppercaseTransformer.class);

	@ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
	public String transform(String payload) {
		return payload.toUpperCase();
	}

	//Following source is used as a test producer.
	@EnableBinding(Source.class)
	static class TestSource {

		private AtomicBoolean semaphore = new AtomicBoolean(true);

		@Bean
		@InboundChannelAdapter(channel = "test-source", poller = @Poller(fixedDelay = "1000"))
		public MessageSource<String> sendTestData() {
			return () ->
					new GenericMessage<>(this.semaphore.getAndSet(!this.semaphore.get()) ? "foo" : "bar");

		}
	}

	//Following sink is used as a test consumer.
	@EnableBinding(Sink.class)
	static class TestSink {

		@StreamListener("test-sink")
		public void receive(String payload) {
			System.out.println("Data received: " + payload);
		}
	}

	public interface Sink {
		@Input("test-sink")
		SubscribableChannel sampleSink();
	}

	public interface Source {
		@Output("test-source")
		MessageChannel sampleSource();
	}
}
