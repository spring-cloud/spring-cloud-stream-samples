/*
 * Copyright 2017-2019 the original author or authors.
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

package org.springframework.cloud.stream.testing.source;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * The Spring Cloud Stream Source application,
 * which generates every second "odd" or "even" string in round-robin manner.
 *
 * @author Artem Bilan
 *
 */
@SpringBootApplication
public class OddEvenSource {

	private AtomicBoolean semaphore = new AtomicBoolean(true);

	@Bean
	public Supplier<String> oddEvenSupplier() {
		return () -> this.semaphore.getAndSet(!this.semaphore.get()) ? "odd" : "even";
	}

	public static void main(String[] args) {
		SpringApplication.run(OddEvenSource.class, args);
	}

}
