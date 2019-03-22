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

package demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.binder.PollableMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Sample app demonstrating a polled consumer where the application can
 * control the rate at which messages are retrieved.
 *
 * @author Gary Russell
 *
 */
@SpringBootApplication
@EnableBinding(PolledConsumerApplication.PolledProcessor.class)
public class PolledConsumerApplication {

	public static final ExecutorService exec = Executors.newSingleThreadExecutor();

	public static void main(String[] args) {
		SpringApplication.run(PolledConsumerApplication.class, args);
	}

	@Bean
	public ApplicationRunner runner(PollableMessageSource input, MessageChannel output) {
		return args -> {
			System.out.println("Send some messages to topic polledConsumerIn and receive from polledConsumerOut");
			System.out.println("Messages will be processed one per second");
			exec.execute(() -> {
				boolean result = false;
				while (true) {
					// this is where we poll for a message, process it, and send a new one
					result = input.poll(m -> {
						String payload = (String) m.getPayload();
						System.out.println("Received: " + payload);
						output.send(MessageBuilder.withPayload(payload.toUpperCase())
							.copyHeaders(m.getHeaders())
							.build());
					}, new ParameterizedTypeReference<String>() { });

					try {
						Thread.sleep(1_000);
					}
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
					if (result) {
						System.out.println("Success");
					}
				}
			});
		};
	}

	public interface PolledProcessor {

		@Input
		PollableMessageSource input();

		@Output
		MessageChannel output();

	}

}
