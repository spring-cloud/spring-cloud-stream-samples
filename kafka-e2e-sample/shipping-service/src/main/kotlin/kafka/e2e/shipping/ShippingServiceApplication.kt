package kafka.e2e.shipping

import kafka.e2e.shipping.stream.ShippingKStreamProcessor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding

@EnableBinding(ShippingKStreamProcessor::class)
@SpringBootApplication
class SpringBootShippingServiceApplication

fun main(args: Array<String>) {
    runApplication<SpringBootShippingServiceApplication>(*args)
}
