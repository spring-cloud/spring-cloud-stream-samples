/*
 * Copyright 2019 the original author or authors.
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
package kafka.streams.inventory.count;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import kafka.streams.inventory.count.KafkaStreamsInventoryCountApplication.KafkaStreamsInventoryAggregator;
import kafka.streams.inventory.count.generator.TopologyTestDriverUpdateEventGenerator;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;

import static kafka.streams.inventory.count.KafkaStreamsInventoryCountApplication.STORE_NAME;

/**
 * A test implementation that uses {@link TopologyTestDriver}. There is no Spring configuration or embedded Kafka broker
 * here so the execution time is very fast. The process method is invoked directly and everything is run in a single thread.
 *
 *
 *
 * @author David Turanski
 */
public class TopolologyTestDriverKafkaStreamsInventoryCountTests extends AbstractInventoryCountTests {

    static final String INPUT_TOPIC = "inventory-update-events";
    static final String OUTPUT_TOPIC = "inventory-count-events";

    private Serde<InventoryCountEvent> countEventSerde = new JsonSerde<>(InventoryCountEvent.class);
    private Serde<InventoryUpdateEvent> updateEventSerde = new JsonSerde<>(InventoryUpdateEvent.class);
    private Serde<ProductKey> keySerde = new JsonSerde<>(ProductKey.class);

    private TopologyTestDriver testDriver;

    static Properties getStreamsConfiguration() {
        final Properties streamsConfiguration = new Properties();
        // Need to be set even these do not matter with TopologyTestDriver
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "TopologyTestDriver");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "ignored");
        return streamsConfiguration;
    }

    private void configureDeserializer(Deserializer<?> deserializer, Class<?> keyDefaultType, Class<?> valueDefaultType, boolean isKey) {
        Map<String, Object> deserializerConfig = new HashMap<>();
        deserializerConfig.put(JsonDeserializer.KEY_DEFAULT_TYPE, keyDefaultType);
        deserializerConfig.put(JsonDeserializer.VALUE_DEFAULT_TYPE, valueDefaultType);
        deserializer.configure(deserializerConfig, isKey);
    }

    @BeforeEach
     void setup() {
        configureDeserializer(countEventSerde.deserializer(), ProductKey.class, InventoryCountEvent.class, false);
        configureDeserializer(keySerde.deserializer() ,ProductKey.class, null, true);

        final StreamsBuilder builder = new StreamsBuilder();

        KStream<ProductKey, InventoryUpdateEvent> input = builder.stream(INPUT_TOPIC, Consumed.with(keySerde, updateEventSerde));
        KafkaStreamsInventoryAggregator inventoryAggregator = new KafkaStreamsInventoryAggregator(Stores.inMemoryKeyValueStore(STORE_NAME));

        KStream<ProductKey, InventoryCountEvent> output = inventoryAggregator.process().apply(input);
        output.to(OUTPUT_TOPIC);

        Topology topology = builder.build();
        testDriver = new TopologyTestDriver(topology, getStreamsConfiguration());

        logger.debug(topology.describe().toString());


        setEventGenerator(new TopologyTestDriverUpdateEventGenerator(testDriver, INPUT_TOPIC, keySerde.serializer(),
                updateEventSerde.serializer()));
    }

    @AfterEach
    void tearDown() {
        super.tearDown();
        try {
            testDriver.close();
        } catch (final RuntimeException e) {
            // https://issues.apache.org/jira/browse/KAFKA-6647 causes exception when executed in Windows, ignoring it
            // Logged stacktrace cannot be avoided
            System.out.println("Ignoring exception, test failing in Windows due this exception:" + e.getLocalizedMessage());
        }
    }

    @Override
    protected Map<ProductKey, InventoryCountEvent> consumeActualInventoryCountEvents(int expectedCount) {
        Map<ProductKey, InventoryCountEvent> inventoryCountEvents = new LinkedHashMap<>();
        int receivedCount = 0;
        while (receivedCount < expectedCount) {
            ProducerRecord<ProductKey, InventoryCountEvent> record
                    = testDriver.readOutput(OUTPUT_TOPIC, keySerde.deserializer(), countEventSerde.deserializer());
            if (record == null) {
                break;
            }
            receivedCount++;
            logger.debug("consumed " + record.key().getProductCode() + " = " + record.value().getCount());
            inventoryCountEvents.put(record.key(), record.value());
        }
        return inventoryCountEvents;
    }

}
