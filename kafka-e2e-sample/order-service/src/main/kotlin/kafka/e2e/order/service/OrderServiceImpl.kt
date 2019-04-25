package kafka.e2e.order.service

import kafka.e2e.order.dto.Order
import kafka.e2e.order.producer.OrderProducer
import org.springframework.stereotype.Service

@Service
class OrderServiceImpl(private val orderProducer: OrderProducer) : OrderService {

    override fun save(order: Order) {
        orderProducer.publishOrderCreatedEvent(order)
    }
}
