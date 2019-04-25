package kafka.e2e.shipping.stream

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import kafka.e2e.customer.Customer
import kafka.e2e.order.OrderCreatedEvent
import kafka.e2e.shipping.OrderShippedEvent
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.handler.annotation.SendTo


@Suppress("UNCHECKED_CAST")
@Configuration
class ShippingKStreamConfiguration {


    @StreamListener
    @SendTo("output")
    fun process(@Input("input") input: KStream<Int, Customer>, @Input("order") orderEvent: KStream<Int, OrderCreatedEvent>): KStream<Int, OrderShippedEvent> {

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
                        .withKeySerde(intSerde)
                        .withValueSerde(customerSerde)

        val customerTable: KTable<Int, Customer> = input.groupByKey(Serialized.with(intSerde, customerSerde))
                .reduce({ _, y -> y }, stateStore)

        return (orderEvent.filter { _, value -> value is OrderCreatedEvent && value.id != 0 }
                .selectKey { _, value -> value.customerId } as KStream<Int, OrderCreatedEvent>)
                .join(customerTable, { orderIt, customer ->
                    OrderShippedEvent(orderIt.id, orderIt.productId, customer.name, customer.address)
                }, Joined.with(intSerde, orderCreatedSerde, customerSerde))
                .selectKey { _, value -> value.id }
    }

}
