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

package io.spring.example.couchbase.sink.integration;

import java.time.Duration;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.kv.ExistsResult;
import io.spring.example.couchbase.sink.domain.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.couchbase.CouchbaseService;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.app.test.integration.StreamAppContainer;
import org.springframework.cloud.stream.app.test.integration.TestTopicSender;
import org.springframework.cloud.stream.app.test.integration.junit.jupiter.KafkaStreamAppTest;
import org.springframework.cloud.stream.app.test.integration.kafka.KafkaConfig;
import org.springframework.cloud.stream.app.test.integration.kafka.KafkaStreamAppContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.cloud.stream.app.test.integration.AppLog.appLog;

@KafkaStreamAppTest
@Tag("integration")
public class CouchbaseSinkIntegrationTests {

	static StreamAppContainer sink = new KafkaStreamAppContainer("couchbase-sink:0.0.1-SNAPSHOT");

	@Container
	static CouchbaseContainer container = new CouchbaseContainer("couchbase/server:6.6.0")
			.withEnabledServices(CouchbaseService.INDEX, CouchbaseService.KV, CouchbaseService.QUERY,
					CouchbaseService.SEARCH)
			.withNetwork(KafkaConfig.kafka.getNetwork())
			.withNetworkAliases("couchbase-server")
			.withBucket(new BucketDefinition("test"));

	static Cluster cluster;

	@Autowired
	TestTopicSender testTopicSender;

	@BeforeAll
	static void initialize() {
		await().until(() -> container.isRunning());
		String connectionString = "couchbase://couchbase-server";
		sink.waitingFor(Wait.forLogMessage(".*Started CouchbaseSink.*", 1))
				.withLogConsumer(appLog("couchbase-sink"))
				.withCommand(
						"--spring.couchbase.connection-string=couchbase://couchbase-server",
						"--spring.couchbase.username=" + container.getUsername(),
						"--spring.couchbase.password=" + container.getPassword(),
						"--couchbase.consumer.bucket-expression='test'",
						"--couchbase.consumer.key-expression=payload.email")
				.start();

		cluster = Cluster.connect(container.getConnectionString(),
				ClusterOptions.clusterOptions(container.getUsername(), container.getPassword()));
	}

	@AfterAll
	static void stop() {
		sink.stop();
	}

	@Test
	void test() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		testTopicSender.send(sink.getInputDestination(),
				objectMapper.writeValueAsString(new User("Bart Simpson", "bart@simpsons.com")));

		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
			ExistsResult result = cluster.bucket("test").defaultCollection().exists("bart@simpsons.com");
			assertThat(result.exists()).isTrue();
		});

		User user = objectMapper.readValue(
				cluster.bucket("test").defaultCollection().get("bart@simpsons.com").contentAs(String.class),
				User.class);

		assertThat(user.getName()).isEqualTo("Bart Simpson");
	}

}
