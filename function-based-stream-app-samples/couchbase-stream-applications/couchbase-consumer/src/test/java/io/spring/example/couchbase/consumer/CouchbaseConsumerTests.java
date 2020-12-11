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

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.PreDestroy;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.kv.MutationResult;
import io.spring.example.couchbase.consumer.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Testcontainers
public class CouchbaseConsumerTests {

	@Container
	static CouchbaseContainer container = new CouchbaseContainer("couchbase/server:6.6.0")
			.withBucket(new BucketDefinition("test"));

	private ApplicationContextRunner applicationContextRunner;

	@BeforeEach
	void setup() {
		applicationContextRunner = new ApplicationContextRunner()
				.withUserConfiguration(TestConfig.class)
				.withPropertyValues(
						"spring.couchbase.connection-string=" + container.getConnectionString(),
						"spring.couchbase.username=" + container.getUsername(),
						"spring.couchbase.password=" + container.getPassword());
	}

	@Test
	void keyExpressionRequired() {
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(
				() -> applicationContextRunner.withPropertyValues("couchbase.consumer.bucket-expression='test'")
						.run(context -> context.start()))
				.havingRootCause()
				.withMessageContaining("'keyExpression' is required");
	}

	@Test
	void singleUpsert() {
		applicationContextRunner.withPropertyValues(
				"couchbase.consumer.bucketExpression='test'",
				"couchbase.consumer.keyExpression=payload.email")
				.run(context -> {
					CouchbaseConsumerProperties properties = context.getBean(CouchbaseConsumerProperties.class);
					String bucketName = properties.getBucketExpression().getValue(String.class);
					Cluster cluster = context.getBean(Cluster.class);
					Function<Flux<Message<?>>, Flux<MutationResult>> couchbaseConsumerFunction = context
							.getBean("couchbaseConsumerFunction", Function.class);
					StepVerifier.create(couchbaseConsumerFunction
							.apply(Flux.just(new GenericMessage<>(new User("David", "david@david.com")))))
							.expectNextMatches(
									mutationResult -> mutationResult.mutationToken().get().bucketName().equals(
											bucketName))
							.verifyComplete();

					User saved = cluster.bucket(bucketName).defaultCollection().get("david@david.com")
							.contentAs(User.class);
					assertThat(saved.getName()).isEqualTo("David");
				});
	}

	@Test
	void singleUpsertConsumer() {
		applicationContextRunner.withPropertyValues(
				"couchbase.consumer.bucketExpression='test'",
				"couchbase.consumer.keyExpression=payload.email")
				.run(context -> {
					CouchbaseConsumerProperties properties = context.getBean(CouchbaseConsumerProperties.class);
					Cluster cluster = context.getBean(Cluster.class);
					String bucketName = properties.getBucketExpression().getValue(String.class);
					Consumer<Flux<Message<?>>> couchbaseConsumer = context
							.getBean("couchbaseConsumer", Consumer.class);
					couchbaseConsumer.accept(Flux.just(new GenericMessage<>(new User("David", "david@david.com"))));

					User saved = cluster.bucket(bucketName).defaultCollection().get("david@david.com")
							.contentAs(User.class);
					assertThat(saved.getName()).isEqualTo("David");
				});
	}

	@Test
	void multipleUpsert() {
		applicationContextRunner.withPropertyValues(
				"couchbase.consumer.bucketExpression='test'",
				"couchbase.consumer.keyExpression=payload.email")
				.run(context -> {
					CouchbaseConsumerProperties properties = context.getBean(CouchbaseConsumerProperties.class);
					Cluster cluster = context.getBean(Cluster.class);
					String bucketName = properties.getBucketExpression().getValue(String.class);
					User user1 = new User("David", "david@david.com");
					User user2 = new User("Nanette", "nanette@nanette.com");
					User user3 = new User("Soby", "soby@soby.com");

					Function<Flux<Message<?>>, Flux<MutationResult>> couchbaseConsumerFunction = context
							.getBean("couchbaseConsumerFunction", Function.class);
					StepVerifier.create(couchbaseConsumerFunction
							.apply(Flux.just(
									new GenericMessage<>(user1),
									new GenericMessage<>(user2),
									new GenericMessage<>(user3))))
							.expectNextMatches(
									mutationResult -> mutationResult.mutationToken().get().bucketName().equals(
											bucketName))
							.expectNextMatches(
									mutationResult -> mutationResult.mutationToken().get().bucketName().equals(
											bucketName))
							.expectNextMatches(
									mutationResult -> mutationResult.mutationToken().get().bucketName().equals(
											bucketName))
							.verifyComplete();

					List<User> users = cluster.query("SELECT name,email from test").rowsAs(User.class);
					assertThat(users).containsExactlyInAnyOrder(user1, user2, user3);
				});
	}

	@Test
	void customBucketExpression() {
		applicationContextRunner.withPropertyValues(
				"couchbase.consumer.bucketExpression=headers.bucketName",
				"couchbase.consumer.keyExpression=payload.email")
				.run(context -> {
					CouchbaseConsumerProperties properties = context.getBean(CouchbaseConsumerProperties.class);
					Cluster cluster = context.getBean(Cluster.class);
					String bucketName = "test";
					MessageBuilder.withPayload(new User("David", "david@david.com"))
							.copyHeaders(Collections.singletonMap("bucketName", bucketName)).build();
					Function<Flux<Message<?>>, Flux<MutationResult>> couchbaseConsumerFunction = context
							.getBean("couchbaseConsumerFunction", Function.class);
					Message<?> message = MessageBuilder.withPayload(new User("David", "david@david.com"))
							.copyHeaders(Collections.singletonMap("bucketName", bucketName)).build();
					StepVerifier.create(couchbaseConsumerFunction.apply(Flux.just(message)))
							.expectNextMatches(
									(MutationResult mutationResult) -> mutationResult.mutationToken().get().bucketName()
											.equals(
													bucketName))
							.verifyComplete();

					User saved = cluster.bucket(bucketName).defaultCollection().get("david@david.com")
							.contentAs(User.class);
					assertThat(saved.getName()).isEqualTo("David");
				});
	}

	@Test
	void customValueExpression() {
		applicationContextRunner.withPropertyValues(
				"couchbase.consumer.bucketExpression='test'",
				"couchbase.consumer.valueExpression=payload.user",
				"couchbase.consumer.keyExpression=payload.user.email")
				.run(context -> {
					CouchbaseConsumerProperties properties = context.getBean(CouchbaseConsumerProperties.class);
					Cluster cluster = context.getBean(Cluster.class);
					String bucketName = properties.getBucketExpression().getValue(String.class);
					Function<Flux<Message<?>>, Flux<MutationResult>> couchbaseConsumerFunction = context
							.getBean("couchbaseConsumerFunction", Function.class);
					StepVerifier.create(couchbaseConsumerFunction
							.apply(Flux
									.just(new GenericMessage<>(
											Collections.singletonMap("user", new User("David", "david@david.com"))))))
							.expectNextMatches(
									mutationResult -> mutationResult.mutationToken().get().bucketName().equals(
											bucketName))
							.verifyComplete();

					Bucket bucket = cluster.bucket(bucketName);
					User saved = cluster.bucket(bucketName).defaultCollection().get("david@david.com")
							.contentAs(User.class);
					assertThat(saved.getName()).isEqualTo("David");
				});
	}

	@Test
	void bucketDoesNotExistShouldThrowException() {
		applicationContextRunner.withPropertyValues(
				"couchbase.consumer.bucketExpression='users'",
				"couchbase.consumer.keyExpression=payload.email")
				.run(context -> {
					CouchbaseConsumerProperties properties = context.getBean(CouchbaseConsumerProperties.class);
					Function<Flux<Message<?>>, Flux<MutationResult>> couchbaseConsumerFunction = context
							.getBean("couchbaseConsumerFunction", Function.class);
					StepVerifier.create(couchbaseConsumerFunction
							.apply(Flux.just(new GenericMessage<>(new User("David", "david@david.com")))))
							.expectErrorMatches(e -> e.toString().contains("BUCKET_NOT_AVAILABLE"))
							.verify();
				});
	}

	@SpringBootApplication
	static class TestConfig {
		@Autowired
		Cluster cluster;

		@PreDestroy
		public void destroy() {
			cluster.disconnect();
		}
	}
}
