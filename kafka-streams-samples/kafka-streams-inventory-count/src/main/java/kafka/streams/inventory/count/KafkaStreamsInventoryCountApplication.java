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

import java.util.function.Function;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;


@SpringBootApplication
public class KafkaStreamsInventoryCountApplication {


    final static String STORE_NAME = "inventory-counts";

    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamsInventoryAggregator.class, args);
    }

    @Bean
    public KeyValueBytesStoreSupplier storeSupplier() {
        return Stores.inMemoryKeyValueStore(STORE_NAME);
    }

    public static class KafkaStreamsInventoryAggregator {

        private static final Logger logger = LoggerFactory.getLogger(KafkaStreamsInventoryAggregator.class);

        private final KeyValueBytesStoreSupplier storeSupplier;

        private final InventoryCountUpdateEventUpdater inventoryCountUpdateEventUpdater = new InventoryCountUpdateEventUpdater();

        private final  Serde<InventoryCountEvent> countEventSerde;

        private final Serde<InventoryUpdateEvent> updateEventSerde;

        private final  Serde<ProductKey> keySerde;

        public KafkaStreamsInventoryAggregator(KeyValueBytesStoreSupplier storeSupplier) {
            this.storeSupplier = storeSupplier;
            this.keySerde = new JsonSerde<>(ProductKey.class);
            this.countEventSerde = new JsonSerde<>(InventoryCountEvent.class);
            this.updateEventSerde = new JsonSerde<>(InventoryUpdateEvent.class);
        }

        @Bean
        public Function<KStream<ProductKey, InventoryUpdateEvent>, KStream<ProductKey, InventoryCountEvent>> process() {
            return input -> input
                    .groupByKey(Grouped.with(keySerde, updateEventSerde))
                    .aggregate(InventoryCountEvent::new,
                            (key, updateEvent, summaryEvent) -> inventoryCountUpdateEventUpdater.apply(updateEvent, summaryEvent),
                              Materialized.<ProductKey, InventoryCountEvent>as(storeSupplier)
                                    .withKeySerde(keySerde)
                                    .withValueSerde(countEventSerde))
                    .toStream().peek((k, v) -> logger.debug("aggregated count key {} {}", k.getProductCode(), v.getCount()));
        }
    }
}
