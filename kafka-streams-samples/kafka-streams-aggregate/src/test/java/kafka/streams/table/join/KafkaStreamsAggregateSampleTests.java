package kafka.streams.table.join;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class KafkaStreamsAggregateSampleTests {

	@ClassRule
	public static EmbeddedKafkaRule embeddedKafkaRule = new EmbeddedKafkaRule(1, true, "foobar");

	private static EmbeddedKafkaBroker embeddedKafka = embeddedKafkaRule.getEmbeddedKafka();

	@Autowired
	StreamsBuilderFactoryBean streamsBuilderFactoryBean;

	@LocalServerPort
	int randomServerPort;

	@Before
	public void before() {
		streamsBuilderFactoryBean.setCloseTimeout(0);
	}

	@BeforeClass
	public static void setUp() {
		System.setProperty("spring.cloud.stream.kafka.streams.binder.brokers", embeddedKafka.getBrokersAsString());
	}

	@AfterClass
	public static void tearDown() {
		System.clearProperty("spring.cloud.stream.kafka.streams.binder.brokers");
	}

	@Test
	public void testKafkaStreamsWordCountProcessor() throws Exception {
		Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka);
		ObjectMapper mapper = new ObjectMapper();
		Serde<DomainEvent> domainEventSerde = new JsonSerde<>(DomainEvent.class, mapper);

		senderProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, domainEventSerde.serializer().getClass());

		DefaultKafkaProducerFactory<String, DomainEvent> pf = new DefaultKafkaProducerFactory<>(senderProps);
		try {


			KafkaTemplate<String, DomainEvent> template = new KafkaTemplate<>(pf, true);
			template.setDefaultTopic("foobar");

			DomainEvent ddEvent = new DomainEvent();
			ddEvent.setBoardUuid("12345");
			ddEvent.setEventType("create-domain-event");

			template.sendDefault("", ddEvent);
 			Thread.sleep(1000);
			RestTemplate restTemplate = new RestTemplate();
			String fooResourceUrl
					= "http://localhost:" + randomServerPort + "/events";
			ResponseEntity<String> response
					= restTemplate.getForEntity(fooResourceUrl, String.class);
			assertThat(response.getBody()).contains("create-domain-event");
		}
		finally {
			pf.destroy();
		}
	}

}
