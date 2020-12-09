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

package io.spring.example.couchbase.sink;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.couchbase.client.java.Cluster;
import io.spring.example.couchbase.sink.domain.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.support.GenericMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers
public class CouchbaseSinkApplicationTests {
	@Container
	static CouchbaseContainer container = new CouchbaseContainer("couchbase/server:6.6.0")
			.withBucket(new BucketDefinition("test"));

	static Map<String, Object> connectProperties = new HashMap<>();

	@BeforeAll
	static void initialize() {
		connectProperties.put("spring.couchbase.connection-string", container.getConnectionString());
		connectProperties.put("spring.couchbase.username", container.getUsername());
		connectProperties.put("spring.couchbase.password", container.getPassword());
	}

	@Test
	void test() {
		try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
				TestChannelBinderConfiguration
						.getCompleteConfiguration(CouchbaseSinkApplication.class))
								.web(WebApplicationType.NONE)
								.properties(connectProperties)
								.run("--couchbase.consumer.bucketExpression='test'",
										"--couchbase.consumer.keyExpression=payload.email")) {
			InputDestination inputDestination = context.getBean(InputDestination.class);
			Cluster cluster = context.getBean(Cluster.class);
			inputDestination.send(new GenericMessage<>(new User("Bart Simpson", "bart@simpsons.com")));

			await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
				User user = cluster.bucket("test").defaultCollection().get("bart@simpsons.com")
						.contentAs(User.class);
				assertThat(user).isNotNull();
				assertThat(user.getName()).isEqualTo("Bart Simpson");
			});
		}
	}

}
