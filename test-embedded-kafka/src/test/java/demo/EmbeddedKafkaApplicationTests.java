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

package demo;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class demonstrating how to use an embedded kafka service with the
 * kafka binder.
 *
 * @author Gary Russell
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class EmbeddedKafkaApplicationTests {

	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1);

	@Autowired
	private KafkaTemplate<byte[], byte[]> template;

	@Autowired
	private DefaultKafkaConsumerFactory<byte[], byte[]> consumerFactory;

	@Value("${spring.cloud.stream.bindings.input.destination}")
	private String inputDestination;

	@Value("${spring.cloud.stream.bindings.output.destination}")
	private String outputDestination;

	@BeforeClass
	public static void setup() {
		System.setProperty("spring.kafka.bootstrap-servers", embeddedKafka.getBrokersAsString());
		System.setProperty("spring.cloud.stream.kafka.binder.zkNodes", embeddedKafka.getZookeeperConnectionString());
	}

	@Test
	public void testSendReceive() {
		template.send(this.inputDestination, "foo".getBytes());
		Consumer<byte[], byte[]> consumer = this.consumerFactory.createConsumer();
		consumer.subscribe(Collections.singleton(this.outputDestination));
		ConsumerRecords<byte[], byte[]> records = consumer.poll(10_000);
		consumer.commitSync();
		assertThat(records.count()).isEqualTo(1);
		assertThat(new String(records.iterator().next().value())).isEqualTo("FOO");
	}

}
