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

package kafka.e2e.order.service

import kafka.e2e.order.dto.Order
import kafka.e2e.order.producer.OrderProducer
import org.springframework.stereotype.Service

/**
 * @author José A. Íñigo
 */
@Service
class OrderServiceImpl(private val orderProducer: OrderProducer) : OrderService {

    override fun save(order: Order) {
        orderProducer.publishOrderCreatedEvent(order)
    }
}
