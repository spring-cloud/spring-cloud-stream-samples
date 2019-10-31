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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import kafka.streams.inventory.count.InventoryCountEvent;
import kafka.streams.inventory.count.InventoryCountUpdateEventUpdater;
import kafka.streams.inventory.count.InventoryUpdateEvent;
import kafka.streams.inventory.count.ProductKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static kafka.streams.inventory.count.InventoryUpdateEvent.Action.DEC;
import static kafka.streams.inventory.count.InventoryUpdateEvent.Action.INC;
import static kafka.streams.inventory.count.InventoryUpdateEvent.Action.REP;

/**
 * Base class to generate random {@link InventoryUpdateEvent}s which are aggregated by the stream processor.
 * Subclasses implement 'doSendEvent(key,value)'.
 *
 * @author David Turanski
 */
public abstract class AbstractInventoryUpdateEventGenerator {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<ProductKey, InventoryCountEvent> accumulatedInventoryCounts = new LinkedHashMap<>();

    public Map<ProductKey, InventoryCountEvent> generateRandomEvents(int numberKeys, int eventsPerKey) {
        InventoryUpdateEvent.Action[] actions = {INC, DEC, REP};
        return doGenerateEvents(numberKeys, eventsPerKey, actions);
    }

    /**
     * Resets the Kafka stream materialized state and the internal state by sending a 0 count for each existing key.
     * @return the state prior to invoking this method.
     */
    public Map<ProductKey, InventoryCountEvent> reset() {
        Map<ProductKey, InventoryCountEvent> current
                = Collections.unmodifiableMap(new LinkedHashMap(accumulatedInventoryCounts));

        accumulatedInventoryCounts.keySet().forEach(key -> {
            InventoryUpdateEvent inventoryUpdateEvent = new InventoryUpdateEvent();
            inventoryUpdateEvent.setKey(key);
            inventoryUpdateEvent.setAction(REP);
            inventoryUpdateEvent.setDelta(0);
            sendEvent(key, inventoryUpdateEvent);
        });
        accumulatedInventoryCounts.clear();
        return current;
    }

    /**
     * @param numberKeys   number of keys to generate events for.
     * @param eventsPerKey number of events per key.
     * @param actions      the list of update actions to include.
     * @return expected calculated counts. Accumulates values since last reset to simulate what the aggregator does.
     */
    private Map<ProductKey, InventoryCountEvent> doGenerateEvents(int numberKeys, int eventsPerKey, InventoryUpdateEvent.Action[] actions) {
        Random random = new Random();

        InventoryCountUpdateEventUpdater summaryEventUpdater = new InventoryCountUpdateEventUpdater();

        for (int j = 0; j < numberKeys; j++) {
            ProductKey key = new ProductKey("key" + j);
            InventoryCountEvent inventoryCountEvent = new InventoryCountEvent(key,
                    accumulatedInventoryCounts.containsKey(key) ? accumulatedInventoryCounts.get(key).getCount() : 0);
            for (int i = 0; i < eventsPerKey; i++) {
                InventoryUpdateEvent inventoryUpdateEvent = new InventoryUpdateEvent();
                inventoryUpdateEvent.setKey(key);

                inventoryUpdateEvent.setDelta(random.nextInt(10) + 1);
                inventoryUpdateEvent.setAction(actions[random.nextInt(actions.length)]);

                inventoryCountEvent = summaryEventUpdater.apply(inventoryUpdateEvent, inventoryCountEvent);

                sendEvent(inventoryUpdateEvent.getKey(),inventoryUpdateEvent);

            }
            accumulatedInventoryCounts.put(key, inventoryCountEvent);
        }

        return Collections.unmodifiableMap(new LinkedHashMap<>(accumulatedInventoryCounts));

    }

    protected void sendEvent(ProductKey key, InventoryUpdateEvent value) {
        logger.debug("Sending inventoryUpdateEvent: key {} delta {} action {}",
                key.getProductCode(), value.getDelta(), value.getAction());
       doSendEvent(key, value);
    }

    protected abstract void doSendEvent(ProductKey key, InventoryUpdateEvent value);
}
