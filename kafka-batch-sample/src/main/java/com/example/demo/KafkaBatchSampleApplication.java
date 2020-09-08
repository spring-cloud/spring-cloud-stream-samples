/*
 * Copyright 2020 the original author or authors.
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

package com.example.demo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class KafkaBatchSampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaBatchSampleApplication.class, args);
	}

	@KafkaListener(id = "batch-out", topics = "batch-out")
	public void listen(String in) {
		System.out.println(in);
	}

	@Bean
	public ApplicationRunner runner(KafkaTemplate<byte[], byte[]> template) {
		return args -> IntStream.range(0, 10).forEach(i -> template.send("batch-in", ("\"test" + i + "\"").getBytes()));
	}

}

class Base {

	@Autowired
	StreamBridge bridge;

}

@Component
@Profile("default")
class NoTransactions extends Base {

	@Bean
	Consumer<List<String>> consumer() {
		return list -> list.forEach(str -> bridge.send("output-out-0", str.toUpperCase()));
	}

}

@Component
@Profile("transactional")
class Transactions extends Base {

	@Bean
	Consumer<List<String>> consumer() {
		return list -> {
			list.forEach(str -> bridge.send("output-out-0", str.toUpperCase()));
			System.out.println("Hit Enter to exit the listener and commit transaction");
			try {
				System.in.read();
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		};
	}

}
