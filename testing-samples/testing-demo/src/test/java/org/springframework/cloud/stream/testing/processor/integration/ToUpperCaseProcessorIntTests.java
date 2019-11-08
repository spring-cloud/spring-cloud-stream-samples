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

package org.springframework.cloud.stream.testing.processor.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration;
import org.springframework.cloud.stream.testing.processor.ToUpperCaseProcessor;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

/**
 * A Spring Boot integration test for the Spring Cloud Stream Processor application
 * based on the Embedded Kafka.
 *
 * @author Artem Bilan
 *
 */
@SpringBootTest(
		properties = {
				"spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
				"spring.cloud.stream.bindings.uppercaseFunction-out-0.destination=" + ToUpperCaseProcessorIntTests.TEST_TOPIC_OUT,
				"spring.cloud.stream.bindings.uppercaseFunction-in-0.group=embeddedKafkaApplication",
				"spring.cloud.stream.bindings.uppercaseFunction-in-0.destination=" + ToUpperCaseProcessorIntTests.TEST_TOPIC_IN,
				"spring.kafka.consumer.group-id=EmbeddedKafkaIntTest"
		},
		classes = ToUpperCaseProcessor.class,
		webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ImportAutoConfiguration(exclude = {
		TestSupportBinderAutoConfiguration.class,
		DataSourceAutoConfiguration.class,
		TransactionAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class })
@EmbeddedKafka(controlledShutdown = true,
		bootstrapServersProperty = "spring.kafka.bootstrap-servers",
		topics = ToUpperCaseProcessorIntTests.TEST_TOPIC_OUT)
@DirtiesContext
class ToUpperCaseProcessorIntTests {

	static final String TEST_TOPIC_IN = "test_topic_in";

	static final String TEST_TOPIC_OUT = "test_topic_out";

	@Autowired
	private KafkaTemplate<byte[], byte[]> template;

	@Autowired
	private DefaultKafkaConsumerFactory<byte[], String> consumerFactory;

	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;

	@Test
	void testMessagesOverKafka() {
		this.template.send(TEST_TOPIC_IN, "test".getBytes());

		Consumer<byte[], String> consumer = this.consumerFactory.createConsumer();

		embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, TEST_TOPIC_OUT);

		ConsumerRecords<byte[], String> replies = KafkaTestUtils.getRecords(consumer);
		assertThat(replies.count()).isEqualTo(1);

		Iterator<ConsumerRecord<byte[], String>> iterator = replies.iterator();
		assertThat(iterator.next().value()).isEqualTo("TEST");
	}

}
