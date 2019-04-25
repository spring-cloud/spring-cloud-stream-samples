package kafka.e2e.order.producer

import kafka.e2e.order.OrderCreatedEvent
import kafka.e2e.order.dto.Order
import org.springframework.cloud.stream.messaging.Source
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component


@Component
class OrderProducer(private val source: Source) {

    fun publishOrderCreatedEvent(order: Order) {
        source.output().send(MessageBuilder.withPayload(OrderCreatedEvent(order.id, order.productId, order.customerId))
                .setHeader(KafkaHeaders.MESSAGE_KEY, order.id).build())
    }

}
