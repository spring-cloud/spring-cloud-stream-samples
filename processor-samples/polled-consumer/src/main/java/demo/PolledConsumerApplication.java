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

	public static void main(String[] args) {
		SpringApplication.run(PolledConsumerApplication.class, args);
	}

	@Bean
	public ApplicationRunner runner(PollableMessageSource input, MessageChannel output) {
		return args -> {
			ExecutorService exec = Executors.newSingleThreadExecutor();
			exec.execute(() -> {
				boolean result = false;
				while (!result) {
					// this is where we poll for a message, process it, and send a new one
					result = input.poll(m -> {
						output.send(MessageBuilder.withPayload(((String) m.getPayload()).toUpperCase())
							.copyHeaders(m.getHeaders())
							.build());
					}, new ParameterizedTypeReference<String>() { });

					if (!result) {
						try {
							Thread.sleep(1_000);
						}
						catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
				}
				System.out.println("Success: " + result);
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
