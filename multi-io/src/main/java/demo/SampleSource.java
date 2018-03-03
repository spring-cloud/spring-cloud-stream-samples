/*
 * Copyright 2016-2018 the original author or authors.
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

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

/**
 * @author Ilayaperumal Gopinathan
 */
@EnableBinding(SampleSource.MultiOutputSource.class)
public class SampleSource {

	@Bean
	@InboundChannelAdapter(value = MultiOutputSource.OUTPUT1, poller = @Poller(fixedDelay = "1000", maxMessagesPerPoll = "1"))
	public synchronized MessageSource<String> messageSource1() {
		return new MessageSource<String>() {
			public Message<String> receive() {
				String message = "FromSource1";
				System.out.println("******************");
				System.out.println("From Source1");
				System.out.println("******************");
				System.out.println("Sending value: " + message);
				return new GenericMessage(message);
			}
		};
	}

	@Bean
	@InboundChannelAdapter(value = MultiOutputSource.OUTPUT2, poller = @Poller(fixedDelay = "1000", maxMessagesPerPoll = "1"))
	public synchronized MessageSource<String> timerMessageSource() {
		return new MessageSource<String>() {
			public Message<String> receive() {
				String message = "FromSource2";
				System.out.println("******************");
				System.out.println("From Source2");
				System.out.println("******************");
				System.out.println("Sending value: " + message);
				return new GenericMessage(message);
			}
		};
	}

	public interface MultiOutputSource {
		String OUTPUT1 = "output1";

		String OUTPUT2 = "output2";

		@Output(OUTPUT1)
		MessageChannel output1();

		@Output(OUTPUT2)
		MessageChannel output2();
	}
}
