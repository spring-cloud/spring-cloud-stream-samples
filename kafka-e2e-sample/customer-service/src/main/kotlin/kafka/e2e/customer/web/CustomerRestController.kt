package kafka.e2e.customer.web

import kafka.e2e.customer.Customer
import kafka.e2e.customer.service.CustomerService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/customers")
class CustomerRestController(private val customerService: CustomerService) {

    @PostMapping
    fun save(@RequestBody customer: Customer) {
        customerService.save(customer)
    }

}
