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

package demo;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author Dave Syer
 * @author Soby Chacko
 */
@SpringBootApplication
public class UppercaseTransformer {

	private static Logger logger = LoggerFactory.getLogger(UppercaseTransformer.class);

	public static void main(String[] args) {
		SpringApplication.run(UppercaseTransformer.class, args);
	}

	@Bean
	public Function<String, String> transform() {
		return payload -> payload.toUpperCase();
	}

	//Following source is used as a test producer.
	static class TestSource {

		private AtomicBoolean semaphore = new AtomicBoolean(true);

		@Bean
		public Supplier<String> sendTestData() {
			return () -> this.semaphore.getAndSet(!this.semaphore.get()) ? "foo" : "bar";

		}
	}

	//Following sink is used as a test consumer.
	static class TestSink {

		@Bean
		public Consumer<String> receive() {
			return payload -> logger.info("Data received: " + payload);
		}
	}
}
