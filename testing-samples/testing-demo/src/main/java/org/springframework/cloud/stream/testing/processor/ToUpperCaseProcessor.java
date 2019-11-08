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

package org.springframework.cloud.stream.testing.processor;

import java.util.function.Function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * The Spring Cloud Stream Processor application,
 * which convert incoming String to its upper case representation.
 *
 * @author Artem Bilan
 *
 */
@SpringBootApplication
public class ToUpperCaseProcessor {

	@Bean
	public Function<String, String> uppercaseFunction() {
		return String::toUpperCase;
	}

	public static void main(String[] args) {
		SpringApplication.run(ToUpperCaseProcessor.class, args);
	}

}
