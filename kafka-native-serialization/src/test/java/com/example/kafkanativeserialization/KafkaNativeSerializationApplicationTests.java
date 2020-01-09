package com.example.kafkanativeserialization;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
public class KafkaNativeSerializationApplicationTests {

	private static final String INPUT_TOPIC = "topic1";
	private static final String OUTPUT_TOPIC = "topic2";
	private static final String GROUP_NAME = "nativeSerializationTest";

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, OUTPUT_TOPIC);

	@BeforeClass
	public static void setup() {
		System.setProperty("spring.cloud.stream.kafka.binder.brokers", embeddedKafka.getEmbeddedKafka().getBrokersAsString());
	}

	@Test
	public void testSendReceive() {
		Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka.getEmbeddedKafka());
		senderProps.put("value.serializer", StringSerializer.class);
		DefaultKafkaProducerFactory<byte[], String> pf = new DefaultKafkaProducerFactory<>(senderProps);
		KafkaTemplate<byte[], String> template = new KafkaTemplate<>(pf, true);
		template.setDefaultTopic(INPUT_TOPIC);
		template.sendDefault("foo");

		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(GROUP_NAME, "false", embeddedKafka.getEmbeddedKafka());
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps.put("value.deserializer", MyJsonDeserializer.class);
		DefaultKafkaConsumerFactory<byte[], Person> cf = new DefaultKafkaConsumerFactory<>(consumerProps);

		Consumer<byte[], Person> consumer = cf.createConsumer();
		consumer.subscribe(Collections.singleton(OUTPUT_TOPIC));
		ConsumerRecords<byte[], Person> records = consumer.poll(Duration.ofSeconds(10));
		consumer.commitSync();

		assertThat(records.count()).isEqualTo(1);
		assertThat(new String(records.iterator().next().value().getName())).isEqualTo("foo");
	}
}
