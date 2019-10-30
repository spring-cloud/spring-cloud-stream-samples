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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MultibinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultibinderApplication.class, args);
	}

	@Bean
	public Function<String, String> process() {
		return payload -> payload.toUpperCase();
	}

	static class TestProducer {

		private AtomicBoolean semaphore = new AtomicBoolean(true);

		@Bean
		public Supplier<String> sendTestData() {
			return () -> this.semaphore.getAndSet(!this.semaphore.get()) ? "foo" : "bar";
		}
	}

	static class TestConsumer {

		private final Log logger = LogFactory.getLog(getClass());

		@Bean
		public Consumer<String> receive() {
			return s -> logger.info("Data received..." + s);
		}
	}

}
