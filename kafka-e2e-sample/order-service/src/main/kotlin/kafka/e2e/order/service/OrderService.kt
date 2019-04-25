package kafka.e2e.order.service

import kafka.e2e.order.dto.Order

interface OrderService {

    fun save(order: Order)

}
