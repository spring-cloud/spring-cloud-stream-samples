package kafka.streams.table.join;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Soby Chacko
 */
public class Producers {

	public static void main(String... args) {

		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer.class);

		List<KeyValue<String, Long>> userClicks = Arrays.asList(
				new KeyValue<>("alice", 13L),
				new KeyValue<>("bob", 4L),
				new KeyValue<>("chao", 25L),
				new KeyValue<>("bob", 19L),
				new KeyValue<>("dave", 56L),
				new KeyValue<>("eve", 78L),
				new KeyValue<>("alice", 40L),
				new KeyValue<>("fang", 99L)
		);

		DefaultKafkaProducerFactory<String, Long> pf = new DefaultKafkaProducerFactory<>(props);
		KafkaTemplate<String, Long> template = new KafkaTemplate<>(pf, true);
		template.setDefaultTopic("user-clicks3");

		for (KeyValue<String,Long> keyValue : userClicks) {
			template.sendDefault(keyValue.key, keyValue.value);
		}

		List<KeyValue<String, String>> userRegions = Arrays.asList(
				new KeyValue<>("alice", "asia"),   /* Alice lived in Asia originally... */
				new KeyValue<>("bob", "americas"),
				new KeyValue<>("chao", "asia"),
				new KeyValue<>("dave", "europe"),
				new KeyValue<>("alice", "europe"), /* ...but moved to Europe some time later. */
				new KeyValue<>("eve", "americas"),
				new KeyValue<>("fang", "asia")
		);

		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		DefaultKafkaProducerFactory<String, String> pf1 = new DefaultKafkaProducerFactory<>(props);
		KafkaTemplate<String, String> template1 = new KafkaTemplate<>(pf1, true);
		template1.setDefaultTopic("user-regions");

		for (KeyValue<String,String> keyValue : userRegions) {
			template1.sendDefault(keyValue.key, keyValue.value);
		}

	}

}
