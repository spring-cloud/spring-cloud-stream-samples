/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.cloud.stream.testing.processor.integration;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.testing.processor.ToUpperCaseProcessor;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Spring Boot integration test for the Spring Cloud Stream Processor application
 * based on the Embedded Kafka.
 *
 * @author Artem Bilan
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
		properties = {
				"spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration",
				"spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
				"spring.cloud.stream.bindings.output.producer.headerMode=raw",
				"spring.cloud.stream.bindings.input.consumer.headerMode=raw",
				"spring.cloud.stream.bindings.input.group=embeddedKafkaApplication",
				"spring.kafka.consumer.group-id=EmbeddedKafkaIntTest"
		},
		classes = ToUpperCaseProcessor.class,
		webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
@Ignore
public class ToUpperCaseProcessorIntTests {

	@ClassRule
	public static KafkaEmbedded kafkaEmbedded = new KafkaEmbedded(1, true, "output");

	@Autowired
	private KafkaTemplate<byte[], byte[]> template;

	@Autowired
	private DefaultKafkaConsumerFactory<byte[], String> consumerFactory;

	@BeforeClass
	public static void setup() {
		System.setProperty("spring.kafka.bootstrap-servers", kafkaEmbedded.getBrokersAsString());
		System.setProperty("spring.cloud.stream.kafka.binder.zkNodes", kafkaEmbedded.getZookeeperConnectionString());
	}

	@Test
	public void testMessagesOverKafka() throws Exception {
		this.template.send("input", "bar".getBytes());

		Consumer<byte[], String> consumer = this.consumerFactory.createConsumer();

		kafkaEmbedded.consumeFromAnEmbeddedTopic(consumer, "output");

		ConsumerRecords<byte[], String> replies = KafkaTestUtils.getRecords(consumer);
		assertThat(replies.count()).isEqualTo(1);

		Iterator<ConsumerRecord<byte[], String>> iterator = replies.iterator();
		assertThat(iterator.next().value()).isEqualTo("BAR");
	}

}
