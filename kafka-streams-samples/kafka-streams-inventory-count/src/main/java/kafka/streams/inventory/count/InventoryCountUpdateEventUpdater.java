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

import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Function to apply a {@link InventoryUpdateEvent} to an existing {@link InventoryCountEvent}.
 *
 * This is used by the stream processor and also by the test harness to compute the expected count, given a sequence of generated events.
 *
 * @author David Turanski
 */
public class InventoryCountUpdateEventUpdater implements BiFunction<InventoryUpdateEvent, InventoryCountEvent, InventoryCountEvent> {
    private final static Logger logger = LoggerFactory.getLogger(InventoryCountUpdateEventUpdater.class);

    @Override
    public InventoryCountEvent apply(InventoryUpdateEvent inventoryUpdateEvent, InventoryCountEvent inventoryCountEvent) {
        int delta = inventoryUpdateEvent.getDelta();
        logger.trace("Applying update {} {} {} to inventoryCountEvent. Current count is {}",
                inventoryUpdateEvent.getKey().getProductCode(), inventoryUpdateEvent.getAction(), inventoryUpdateEvent.getDelta(), inventoryCountEvent.getCount());
        inventoryCountEvent.setKey(inventoryUpdateEvent.getKey());
        switch (inventoryUpdateEvent.getAction()) {
            case DEC:
                inventoryCountEvent.setCount(inventoryCountEvent.getCount() - delta);
                break;
            case INC:
                inventoryCountEvent.setCount(inventoryCountEvent.getCount() + delta);
                break;
            case REP:
                inventoryCountEvent.setCount(delta);
                break;
            default:
                return null;
        }
        logger.trace("Applied update {} {} {} to inventoryCountEvent. Current count is {}",
                inventoryUpdateEvent.getKey().getProductCode(), inventoryUpdateEvent.getAction(), inventoryUpdateEvent.getDelta(), inventoryCountEvent.getCount());
        return inventoryCountEvent;
    }
}
