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

package demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test class demonstrating how to use an embedded kafka service with the
 * kafka binder to test the {@link PolledConsumerApplication}.
 *
 * @author Gary Russell
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class PolledConsumerApplicationTests {

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, "input", "output");


	@Value("${spring.cloud.stream.bindings.input.destination}")
	private String inputTopic;

	@Value("${spring.cloud.stream.bindings.output.destination}")
	private String outputTopic;

	@BeforeClass
	public static void setup() {
		System.setProperty("spring.cloud.stream.kafka.binder.brokers", embeddedKafka.getEmbeddedKafka().getBrokersAsString());
	}

	@Test
	public void testSendReceive() throws Exception {
		Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka.getEmbeddedKafka());
		senderProps.put("key.serializer", ByteArraySerializer.class);
		senderProps.put("value.serializer", ByteArraySerializer.class);
		DefaultKafkaProducerFactory<byte[], byte[]> pf = new DefaultKafkaProducerFactory<>(senderProps);
		KafkaTemplate<byte[], byte[]> template = new KafkaTemplate<>(pf, true);
		template.setDefaultTopic(inputTopic);
		template.sendDefault("foo".getBytes());

		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafka.getEmbeddedKafka());
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps.put("key.deserializer", ByteArrayDeserializer.class);
		consumerProps.put("value.deserializer", ByteArrayDeserializer.class);
		DefaultKafkaConsumerFactory<byte[], byte[]> cf = new DefaultKafkaConsumerFactory<>(consumerProps);

		Consumer<byte[], byte[]> consumer = cf.createConsumer();
		consumer.subscribe(Collections.singleton(this.outputTopic));
		ConsumerRecords<byte[], byte[]> records = consumer.poll(10_000);
		consumer.commitSync();

		assertThat(records.count()).isEqualTo(1);
		assertThat(new String(records.iterator().next().value())).isEqualTo("FOO");
		consumer.close();
		pf.destroy();

		PolledConsumerApplication.exec.shutdownNow();
	}

}
