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
import kafka.streams.inventory.count.generator.KafkaTemplateInventoryUpdateEventGenerator;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

/**
 * A test implementation that uses {@link SpringApplicationBuilder} directly, instead of '@SpringBootTest'.
 * The advantage is that the {@link EmbeddedKafkaBroker} can be provided by Junit 5 to an `@BeforeAll` method, and the
 * Spring context is configured accordingly.
 *
 * Note, the base class closes the consumer after each test.
 *
 * @author David Turanski
 */
@EmbeddedKafka(topics = KafkaStreamsInventoryCountTests.INPUT_TOPIC)
@DirtiesContext
public class KafkaStreamsInventoryCountTests extends AbstractInventoryCountTests{

    static final String INPUT_TOPIC = "inventory-update-events";
    static final String OUTPUT_TOPIC = "inventory-count-events";
    private static final String GROUP_NAME = "inventory-count-test";

    private static ConfigurableApplicationContext context;
    private static DefaultKafkaConsumerFactory<ProductKey, InventoryCountEvent> cf;

    @BeforeAll
    public static void init(EmbeddedKafkaBroker embeddedKafka) {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        setEventGenerator(new KafkaTemplateInventoryUpdateEventGenerator(props, INPUT_TOPIC));

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(GROUP_NAME, "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "test");
        consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, KafkaStreamsInventoryCountTests.class.getPackage().getName());
        consumerProps.put(JsonDeserializer.KEY_DEFAULT_TYPE, ProductKey.class);
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, InventoryCountEvent.class);
        consumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, "false");
        cf = new DefaultKafkaConsumerFactory<>(consumerProps);


        /*
         * Disabling caching makes the test run faster, and more consistent behavior with the TopologyTestDriver.
         * More messages are produced on the output topic.
         */
        context = new SpringApplicationBuilder(KafkaStreamsInventoryCountApplication.class)
                .properties(
                        "spring.cloud.stream.kafka.streams.binder.brokers=" + embeddedKafka.getBrokersAsString(),
                        "spring.cloud.stream.kafka.streams.binder.configuration.commit.interval.ms=1000",
                        "spring.cloud.stream.kafka.streams.binder.configuration.cache.max.bytes.buffering=0")
                .run();
    }

    @AfterAll
    public static void shutdown() {
        context.close();
    }

    @BeforeEach
    public void setUp() {
        consumer = cf.createConsumer(GROUP_NAME);
        consumer.subscribe(Collections.singleton(OUTPUT_TOPIC));
    }
}
