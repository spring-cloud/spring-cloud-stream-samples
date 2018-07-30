package sample.consumer;

import com.example.Sensor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.cloud.stream.schema.client.EnableSchemaRegistryClient;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableBinding(KafkaStreamsProcessor.class)
@EnableSchemaRegistryClient
@EnableScheduling
public class CountVersionApplication {

    private static final String STORE_NAME = "prod-id-count-store";
    private final Log logger = LogFactory.getLog(getClass());

    ReadOnlyKeyValueStore<Object, Object> keyValueStore;

    @Autowired
    private InteractiveQueryService queryService;

    public static void main(String[] args) {
        SpringApplication.run(CountVersionApplication.class, args);
    }

    @StreamListener("input")
    @SendTo("output")
    public KStream<String, Long> process(KStream<Object, Sensor> input) {

        return input
            .map((key, value) -> {

                String newKey = "v1";
                if (value.getId().toString().endsWith("v2")) {
                    newKey = "v2";
                }
                return new KeyValue<>(newKey, value);
            })
            .groupByKey()
            .count(Materialized.as(STORE_NAME))
            .toStream();
        //.groupByKey(Serialized.with(Serdes.Integer(), new JsonSerde<>(Sensor.class)))
            /*
            .count(Materialized.<Integer, Long, KeyValueStore<Bytes, byte[]>>as(STORE_NAME)
                .withKeySerde(Serdes.Integer())
                .withValueSerde(Serdes.Long()))
                */
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