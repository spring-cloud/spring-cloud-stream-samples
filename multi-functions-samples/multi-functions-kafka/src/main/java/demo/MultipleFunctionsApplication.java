/*
 * Copyright 2015-2019 the original author or authors.
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

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MultipleFunctionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultipleFunctionsApplication.class, args);
	}

	@Bean
	public Consumer<String> sink1() {
		return message -> {
			System.out.println("******************");
			System.out.println("At Sink1");
			System.out.println("******************");
			System.out.println("Received message " + message);
		};
	}

	@Bean
	public Consumer<String> sink2() {
		return message -> {
			System.out.println("******************");
			System.out.println("At Sink2");
			System.out.println("******************");
			System.out.println("Received message " + message);
		};
	}

	@Bean
	public Supplier<String> source1() {
		return () -> {
			String message = "FromSource1";
			System.out.println("******************");
			System.out.println("From Source1");
			System.out.println("******************");
			System.out.println("Sending value: " + message);
			return message;

		};
	}

	@Bean
	public Supplier<String> source2() {
		return () -> {
			String message = "FromSource2";
			System.out.println("******************");
			System.out.println("From Source2");
			System.out.println("******************");
			System.out.println("Sending value: " + message);
			return message;

		};
	}

}
