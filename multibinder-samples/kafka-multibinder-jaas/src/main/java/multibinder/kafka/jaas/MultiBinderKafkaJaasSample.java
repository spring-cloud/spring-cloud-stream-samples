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

package multibinder.kafka.jaas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.handler.annotation.SendTo;

@SpringBootApplication
public class MultiBinderKafkaJaasSample {

	public static void main(String[] args) {
		SpringApplication.run(MultiBinderKafkaJaasSample.class, args);
	}

	@EnableBinding(CustomProcessor.class)
	static class Foo  {

		@StreamListener("input")
		@SendTo("output")
		public String receive(String foo) {
			return foo;
		}

		@StreamListener("input1")
		@SendTo("output1")
		public String receive1(String foo) {
			return foo;
		}
	}

	interface CustomProcessor {

		@Input("input")
		SubscribableChannel input();

		@Output("output")
		MessageChannel output();

		@Input("input1")
		SubscribableChannel input1();

		@Output("output1")
		MessageChannel output1();

	}
}
