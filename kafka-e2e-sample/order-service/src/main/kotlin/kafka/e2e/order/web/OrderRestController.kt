package kafka.e2e.order.web

import kafka.e2e.order.dto.Order
import kafka.e2e.order.service.OrderService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderRestController(private val orderService: OrderService) {

    @PostMapping
    fun save(@RequestBody order: Order) {
        return orderService.save(order)
    }

}
