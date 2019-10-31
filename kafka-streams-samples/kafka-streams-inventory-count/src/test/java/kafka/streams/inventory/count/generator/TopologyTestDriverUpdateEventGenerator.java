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
package kafka.streams.inventory.count.generator;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import kafka.streams.inventory.count.InventoryUpdateEvent;
import kafka.streams.inventory.count.ProductKey;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;

/**
 * Test data generator using {@link InventoryUpdateEvent}s using {@link TopologyTestDriver} to send events.
 * Used for testing with {@link TopologyTestDriver}.
 *
 * @author David Turanski
 */
public class TopologyTestDriverUpdateEventGenerator extends AbstractInventoryUpdateEventGenerator {

    private final TopologyTestDriver topologyTestDriver;
    private final ConsumerRecordFactory<ProductKey, InventoryUpdateEvent> recordFactory;

    public TopologyTestDriverUpdateEventGenerator(TopologyTestDriver topologyTestDriver,
                                                  String inputTopic,
                                                  Serializer<ProductKey> keySerializer,
                                                  Serializer<InventoryUpdateEvent> valueSerializer) {
        this.topologyTestDriver = topologyTestDriver;
        this.recordFactory = new ConsumerRecordFactory<>(
                 inputTopic, keySerializer, valueSerializer);
    }

    @Override
    protected void doSendEvent(ProductKey key, InventoryUpdateEvent value) {
        ConsumerRecord<byte[], byte[]> record = recordFactory.create(key, value, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        topologyTestDriver.pipeInput(record);
    }
}
