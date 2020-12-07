/*
 * Copyright 2020-2020 the original author or authors.
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

package io.spring.example.couchbase.consumer;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ReactiveBucket;
import com.couchbase.client.java.ReactiveCollection;
import com.couchbase.client.java.kv.MutationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;

@Configuration
@EnableConfigurationProperties(CouchbaseConsumerProperties.class)
public class CouchbaseConsumerConfiguration {

	private static Logger logger = LoggerFactory.getLogger(CouchbaseConsumerConfiguration.class);

	@Bean
	public Consumer<Flux<Message<?>>> couchbaseConsumer(
			Function<Flux<Message<?>>, Flux<MutationResult>> couchbaseConsumerFunction) {
		return message -> couchbaseConsumerFunction.apply(message)
				.subscribe(mutationResult ->
						logger.debug("Processed " + message));
	}

	@Bean
	public Function<Flux<Message<?>>, Flux<MutationResult>> couchbaseConsumerFunction(Cluster cluster,
			CouchbaseConsumerProperties consumerProperties) {
		return flux -> flux.flatMap(message -> {
			logger.debug("Processing message " + message);
			String bucketName = bucket(message, consumerProperties.getBucketExpression());
			String key = key(message, consumerProperties.getKeyExpression());
			ReactiveBucket bucket = cluster.bucket(bucketName).reactive();
			ReactiveCollection collection = collection(message, consumerProperties.getCollectionExpression())
					.map(name -> bucket.collection(name)).orElse(bucket.defaultCollection());
			return collection.upsert(key, value(message, consumerProperties.getValueExpression()));
		});
	}

	private String bucket(Message<?> message, Expression expression) {
		return expression.getValue(message, String.class);
	}

	private String key(Message<?> message, Expression expression) {
		return expression.getValue(message, String.class);
	}

	private Object value(Message<?> message, Expression expression) {
		return expression.getValue(message);
	}

	private Optional<String> collection(Message<?> message, @Nullable Expression expression) {
		return expression == null ? Optional.empty() : Optional.of(expression.getValue(message, String.class));
	}
}
