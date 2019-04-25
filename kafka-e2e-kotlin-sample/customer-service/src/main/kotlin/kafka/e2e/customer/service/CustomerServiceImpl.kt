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

package kafka.e2e.customer.service

import kafka.e2e.customer.Customer
import org.springframework.cloud.stream.messaging.Source
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

/**
 * @author José A. Íñigo
 */
@Service
class CustomerServiceImpl(private val customerKafkaProducer: Source) : CustomerService {

    override fun save(customer: Customer) {
        val message = MessageBuilder.withPayload(customer).setHeader(KafkaHeaders.MESSAGE_KEY, customer.getId()).build()
        customerKafkaProducer.output().send(message)
    }
}
