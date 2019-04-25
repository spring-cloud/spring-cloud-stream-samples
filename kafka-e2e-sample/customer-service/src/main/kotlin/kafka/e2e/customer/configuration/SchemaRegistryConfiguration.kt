package kafka.e2e.customer.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.cloud.stream.schema.client.ConfluentSchemaRegistryClient
import org.springframework.cloud.stream.schema.client.SchemaRegistryClient
import org.springframework.context.annotation.Bean


@Configuration
class SchemaRegistryConfiguration {

    @Bean
    fun schemaRegistryClient(@Value("\${spring.cloud.stream.schema-registry-client.endpoint}") endpoint: String): SchemaRegistryClient {
        val client = ConfluentSchemaRegistryClient()
        client.setEndpoint(endpoint)
        return client
    }

}
