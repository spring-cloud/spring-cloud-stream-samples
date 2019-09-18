package sample.consumer;

import com.example.Sensor;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

@SpringBootApplication
@EnableScheduling
public class CountVersionApplication {

	private static final String STORE_NAME = "sensor-store";

	private final Log logger = LogFactory.getLog(getClass());

	ReadOnlyKeyValueStore<Object, Object> keyValueStore;

	@Autowired
	private InteractiveQueryService queryService;

	public static void main(String[] args) {
		SpringApplication.run(CountVersionApplication.class, args);
	}

	@Bean
	public Function<KStream<Object, Sensor>, KStream<String, Long>> process() {

		//The following Serde definitions are not needed in the topoloyy below
		//as we are not using it. However, if your topoloyg explicitly uses this
		//Serde, you need to configure this with the schema registry url as below.

		final Map<String, String> serdeConfig = Collections.singletonMap(
				AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

		final SpecificAvroSerde<Sensor> sensorSerde = new SpecificAvroSerde<>();
		sensorSerde.configure(serdeConfig, false);

		return input -> input
				.map((k, value) -> {
					String newKey = "v1";
					if (value.getId().toString().endsWith("v2")) {
						newKey = "v2";
					}
					return new KeyValue<>(newKey, value);
				})
				.groupByKey()
				.count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(STORE_NAME)
						.withKeySerde(Serdes.String())
						.withValueSerde(Serdes.Long()))
				.toStream();

	}

	@Scheduled(fixedRate = 30000, initialDelay = 5000)
	public void printVersionCounts() {
		if (keyValueStore == null) {
			keyValueStore = queryService.getQueryableStore(STORE_NAME, QueryableStoreTypes.keyValueStore());
		}

		logger.info("Count for v1 is=" + keyValueStore.get("v1"));
		logger.info("Count for v2 is=" + keyValueStore.get("v2"));
	}

}