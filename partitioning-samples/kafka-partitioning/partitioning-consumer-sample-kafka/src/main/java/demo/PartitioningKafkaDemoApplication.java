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

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

@SpringBootApplication
public class PartitioningKafkaDemoApplication {

	private static final Logger logger = LoggerFactory.getLogger(PartitioningKafkaDemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(PartitioningKafkaDemoApplication.class, args);
	}

	@Bean
	public Consumer<Message<String>> listen() {
		return message -> logger.info(message.getPayload() + " received from partition "
						+ message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID));
	}
}
