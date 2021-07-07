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

package multibinder.kafka.jaas;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MultiBinderKafkaJaasSample {

	public static void main(String[] args) {
		SpringApplication.run(MultiBinderKafkaJaasSample.class, args);
	}

	static class Foo {

		Random random = new Random();

		@Bean
		public Function<String, String> receive() {
			return String::toUpperCase;
		}

		@Bean
		public Function<String, String> receive1() {
			return foo -> foo;
		}

		@Bean
		public Supplier<String> supply() {
			return () -> "foo-" + random.nextInt();
		}

		@Bean
		public Consumer<String> consume() {
			return System.out::println;
		}
	}

}