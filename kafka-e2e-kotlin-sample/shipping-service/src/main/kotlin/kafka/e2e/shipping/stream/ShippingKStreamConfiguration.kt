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

package kafka.e2e.shipping.stream

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import kafka.e2e.customer.Customer
import kafka.e2e.order.OrderCreatedEvent
import kafka.e2e.shipping.OrderShippedEvent
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.handler.annotation.SendTo

/**
 * @author José A. Íñigo
 */
@Suppress("UNCHECKED_CAST")
@Configuration
class ShippingKStreamConfiguration {

    @StreamListener
    @SendTo("output")
    fun process(@Input("input") input: KStream<Int, Customer>, @Input("order") orderEvent: KStream<Int, GenericRecord>): KStream<Int, OrderShippedEvent> {

        val serdeConfig = mapOf(
                AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to "http://localhost:8081")

        val intSerde = Serdes.IntegerSerde()
        val customerSerde = SpecificAvroSerde<Customer>()
        customerSerde.configure(serdeConfig, false)
        val orderCreatedSerde = SpecificAvroSerde<OrderCreatedEvent>()
        orderCreatedSerde.configure(serdeConfig, false)
        val orderShippedSerde = SpecificAvroSerde<OrderShippedEvent>()
        orderShippedSerde.configure(serdeConfig, false)

        val stateStore: Materialized<Int, Customer, KeyValueStore<Bytes, ByteArray>> =
                Materialized.`as`<Int, Customer, KeyValueStore<Bytes, ByteArray>>("customer-store")

        val customerTable: KTable<Int, Customer> = input.groupByKey()
                .reduce({ _, y -> y }, stateStore)

        return (orderEvent.filter { _, value -> value.schema.name == "OrderCreatedEvent" }
                .map { key, value -> KeyValue(key, OrderCreatedEvent(value.get("id") as Int, value.get("productId") as Int, value.get("customerId") as Int)) }
                .selectKey { _, value -> value.customerId } as KStream<Int, OrderCreatedEvent>)
                .join(customerTable, { orderIt, customer ->
                    OrderShippedEvent(orderIt.id, orderIt.productId, customer.name, customer.address)
                }, Joined.with(intSerde, orderCreatedSerde, customerSerde))
                .selectKey { _, value -> value.id }
    }

}
