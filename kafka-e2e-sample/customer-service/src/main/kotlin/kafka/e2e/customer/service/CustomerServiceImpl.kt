package kafka.e2e.customer.service

import kafka.e2e.customer.Customer
import org.springframework.cloud.stream.messaging.Source
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class CustomerServiceImpl(private val customerKafkaProducer: Source) : CustomerService {

    override fun save(customer: Customer) {
        val message = MessageBuilder.withPayload(customer).setHeader(KafkaHeaders.MESSAGE_KEY, customer.getId()).build()
        customerKafkaProducer.output().send(message)
    }
}
