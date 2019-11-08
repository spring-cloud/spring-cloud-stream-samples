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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import kafka.streams.inventory.count.generator.AbstractInventoryUpdateEventGenerator;
import kafka.streams.inventory.count.generator.KafkaTemplateInventoryUpdateEventGenerator;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;


/**
 * A test implementation annotated with '@SpringBootTest'.
 *
 * The Spring context is implicitly created and is auto configured to use {@link EmbeddedKafkaBroker}, configured with the required `bootStrapServers` property.
 * Here the EmbeddedKafkaBroker must be autowired as a instance variable, so not available for a static '@BeforeAll' method.
 * Consequently, the {@link DefaultKafkaConsumerFactory} which depends on the broker is configured in `@BeforeEach`.
 *
 * Note, the base class closes the consumer after each test.
 *
 * @author David Turanski
 */
@EmbeddedKafka(
        bootstrapServersProperty = "spring.kafka.bootstrap-servers",
        topics = {
                SpringBootKafkaStreamsInventoryCountTests.INPUT_TOPIC
        })
/*
 * Disabling caching makes the test run faster, and more consistent behavior with the TopologyTestDriver.
 * More messages are produced on the output topic.
 */
@SpringBootTest(
        properties = {
                "spring.cloud.stream.kafka.streams.binder.configuration.commit.interval.ms=1000",
                "spring.cloud.stream.kafka.streams.binder.configuration.cache.max.bytes.buffering=0"
        })
@DirtiesContext
public class SpringBootKafkaStreamsInventoryCountTests extends AbstractInventoryCountTests {

    static final String INPUT_TOPIC = "inventory-update-events";
    static final String OUTPUT_TOPIC = "inventory-count-events";
    private static final String GROUP_NAME = "inventory-count-test";

    private DefaultKafkaConsumerFactory<ProductKey, InventoryCountEvent> cf;

    @Autowired
    private EmbeddedKafkaBroker broker;

    @BeforeEach
    void setUp() {

        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker.getBrokersAsString());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        AbstractInventoryUpdateEventGenerator eventGenerator = new
                KafkaTemplateInventoryUpdateEventGenerator(props, INPUT_TOPIC);
        setEventGenerator(eventGenerator);

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(GROUP_NAME, "true", broker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "test");
        consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, KafkaStreamsInventoryCountTests.class.getPackage().getName());
        consumerProps.put(JsonDeserializer.KEY_DEFAULT_TYPE, ProductKey.class);
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, InventoryCountEvent.class);
        consumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, "false");
        cf = new DefaultKafkaConsumerFactory<>(consumerProps);

        consumer = cf.createConsumer(GROUP_NAME);
        consumer.subscribe(Collections.singleton(OUTPUT_TOPIC));
    }
}
