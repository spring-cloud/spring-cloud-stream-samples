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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import kafka.streams.inventory.count.generator.AbstractInventoryUpdateEventGenerator;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base class for running various implementations of aggregation tests.
 * Each test is repeated multiple times to ensure that state is consistent through multiple invocations.
 *
 * @author David Turanski
 */
public abstract class AbstractInventoryCountTests {
    private static final int REPETITION_COUNT = 3;

    private static AbstractInventoryUpdateEventGenerator eventGenerator;

    protected Consumer<ProductKey, InventoryCountEvent> consumer;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     *
     * @param eventGenerator an {@link AbstractInventoryUpdateEventGenerator} implementation.
     */
    protected static void setEventGenerator(AbstractInventoryUpdateEventGenerator eventGenerator) {
        AbstractInventoryCountTests.eventGenerator = eventGenerator;
    }

    @RepeatedTest(REPETITION_COUNT)
    public void processMessagesForSingleKey() {

        Map<ProductKey, InventoryCountEvent> expectedCounts = eventGenerator.generateRandomEvents(1, 3);

        Map<ProductKey, InventoryCountEvent> actualEvents = consumeActualInventoryCountEvents(3);

        assertThat(actualEvents).hasSize(1);

        expectedCounts.forEach((key, value) ->
                assertThat(actualEvents.get(key).getCount()).isEqualTo(value.getCount()));
    }

    @RepeatedTest(REPETITION_COUNT)
    public void processAggregatedEventsForSingleKey() {
        Map<ProductKey, InventoryCountEvent> expectedCounts;
        expectedCounts = eventGenerator.generateRandomEvents(1, 5);

        Map<ProductKey, InventoryCountEvent> originalCount = consumeActualInventoryCountEvents(5);

        expectedCounts.forEach((key, value) ->
                assertThat(originalCount.get(key).getCount()).isEqualTo(value.getCount()));

        expectedCounts = eventGenerator.generateRandomEvents(1, 5);

        Map<ProductKey, InventoryCountEvent> actualCount = consumeActualInventoryCountEvents(5);

        expectedCounts.forEach((key, value) ->
                assertThat(actualCount.get(key).getCount()).isEqualTo(value.getCount()));
    }

    @RepeatedTest(REPETITION_COUNT)
    public void processAggregatedEventsForMultipleKeys() {
        Map<ProductKey, InventoryCountEvent> initialCounts = eventGenerator.generateRandomEvents(10, 5);

        Map<ProductKey, InventoryCountEvent> expectedEvents;
        expectedEvents = consumeActualInventoryCountEvents(50);

        expectedEvents.forEach((key, value) ->
                assertThat(initialCounts.get(key).getCount()).isEqualTo(value.getCount()));

        Map<ProductKey, InventoryCountEvent> updatedCounts = eventGenerator.generateRandomEvents(10, 5);

        expectedEvents = consumeActualInventoryCountEvents(50);

        boolean atLeastOneUpdatedCountIsDifferent = false;

        for (ProductKey key : updatedCounts.keySet()) {
            assertThat(expectedEvents.get(key).getCount()).isEqualTo(updatedCounts.get(key).getCount());
            atLeastOneUpdatedCountIsDifferent = atLeastOneUpdatedCountIsDifferent || !initialCounts.get(key).equals(updatedCounts.get(key));
        }

        //Verify that the expected counts changed from the first round of events.
        assertThat(atLeastOneUpdatedCountIsDifferent).isTrue();
    }

    /**
     * Reset the state by sending 0 count values to the aggregator.
     * These events are also consumed so that subsequent tests do not have to deal with additional events.
     */
    @AfterEach
    void tearDown() {
        Map<ProductKey, InventoryCountEvent> events = eventGenerator.reset();
        consumeActualInventoryCountEvents(events.size());
        if (consumer != null) {
            consumer.close();
        }
    }

    /**
     * Consume the actual events from the output topic.
     * This implementation uses a {@link Consumer}, assuming a (an embedded) Kafka Broker but may be overridden.
     * @param expectedCount the expected number of messages is known. This avoids a timeout delay if all is well.
     *
     * @return the consumed data.
     */
    protected Map<ProductKey, InventoryCountEvent> consumeActualInventoryCountEvents(int expectedCount) {
        Map<ProductKey, InventoryCountEvent> inventoryCountEvents = new LinkedHashMap<>();
        int receivedCount = 0;
        while (receivedCount < expectedCount) {
            ConsumerRecords<ProductKey, InventoryCountEvent> records = KafkaTestUtils.getRecords(consumer, 1000);
            if (records.isEmpty()) {
                logger.error("No more records received. Expected {} received {}.", expectedCount, receivedCount);
                break;
            }
            receivedCount += records.count();
            for (Iterator<ConsumerRecord<ProductKey, InventoryCountEvent>> it = records.iterator(); it.hasNext(); ) {
                ConsumerRecord<ProductKey, InventoryCountEvent> consumerRecord = it.next();
                logger.debug("consumed " + consumerRecord.key().getProductCode() + " = " + consumerRecord.value().getCount());
                inventoryCountEvents.put(consumerRecord.key(), consumerRecord.value());
            }
        }
        return inventoryCountEvents;
    }
}
