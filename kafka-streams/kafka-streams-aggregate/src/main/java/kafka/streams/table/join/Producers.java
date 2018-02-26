package kafka.streams.table.join;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Soby Chacko
 */
public class Producers {

	public static void main(String... args) {

		ObjectMapper mapper = new ObjectMapper();
		Serde<DomainEvent> domainEventSerde = new JsonSerde<>(DomainEvent.class, mapper);


		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, domainEventSerde.serializer().getClass());

		DomainEvent ddEvent = new DomainEvent();
		ddEvent.setBoardUuid("12345");
		ddEvent.setEventType("thisisanevent");

		DefaultKafkaProducerFactory<String, DomainEvent> pf = new DefaultKafkaProducerFactory<>(props);
		KafkaTemplate<String, DomainEvent> template = new KafkaTemplate<>(pf, true);
		template.setDefaultTopic("foobar");

		template.sendDefault("", ddEvent);
	}
}
