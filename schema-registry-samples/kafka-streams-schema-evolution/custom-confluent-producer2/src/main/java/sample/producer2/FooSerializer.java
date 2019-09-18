package sample.producer2;

import com.example.Sensor;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;

import java.util.Collections;
import java.util.Map;

/**
 * @author Soby Chacko
 */
public class FooSerializer extends SpecificAvroSerializer<Sensor> {

	@Override
	public void configure(Map<String, ?> serializerConfig, boolean isSerializerForRecordKeys) {
		final Map<String, String> serdeConfig = Collections.singletonMap(
				AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
		super.configure(serdeConfig, isSerializerForRecordKeys);
	}
}
