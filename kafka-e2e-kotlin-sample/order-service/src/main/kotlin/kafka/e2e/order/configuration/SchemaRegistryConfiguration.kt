/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.e2e.order.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.cloud.stream.schema.client.ConfluentSchemaRegistryClient
import org.springframework.cloud.stream.schema.client.SchemaRegistryClient
import org.springframework.context.annotation.Bean

/**
 * @author José A. Íñigo
 */
@Configuration
class SchemaRegistryConfiguration {

    @Bean
    fun schemaRegistryClient(@Value("\${spring.cloud.stream.schema-registry-client.endpoint}") endpoint: String): SchemaRegistryClient {
        val client = ConfluentSchemaRegistryClient()
        client.setEndpoint(endpoint)
        return client
    }

}
