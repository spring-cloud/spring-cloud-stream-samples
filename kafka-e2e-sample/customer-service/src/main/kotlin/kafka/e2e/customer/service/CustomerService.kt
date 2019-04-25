package kafka.e2e.customer.service

import kafka.e2e.customer.Customer

interface CustomerService {

    fun save(customer: Customer)

}
