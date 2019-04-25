package kafka.e2e.shipping.stream

import kafka.e2e.customer.Customer
import kafka.e2e.order.OrderCreatedEvent
import kafka.e2e.shipping.OrderShippedEvent
import org.apache.kafka.streams.kstream.KStream
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output

interface ShippingKStreamProcessor {

    @Input("input")
    fun input(): KStream<Int, Customer>

    @Input("order")
    fun order(): KStream<String, OrderCreatedEvent>

    @Output("output")
    fun output(): KStream<String, OrderShippedEvent>

}
