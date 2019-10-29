/*
 * Copyright 2018-2019 the original author or authors.
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

package kinesis.webflux;

import static org.assertj.core.api.Assumptions.assumeThat;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextResourceLoaderAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextStackAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.metadata.SimpleMetadataStore;
import org.springframework.integration.support.locks.DefaultLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * @author Artem Bilan
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {
				"spring.cloud.stream.bindings.kinesisSource-out-0.destination = SSE_DATA",
				"spring.cloud.stream.bindings.kinesisSource-out-0.producer.headerMode = none",
				"spring.cloud.function.definition=kinesisSink;kinesisSource"
		}
)
@AutoConfigureWebTestClient
public class CloudStreamKinesisToWebfluxApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private KinesisTestConfiguration kinesisTestConfiguration;

	@BeforeAll
	static void setup() {
		boolean isLocalKinesis;
		try {
			new Socket("localhost", KinesisTestConfiguration.DEFAULT_KINESALITE_PORT).close();
			// Successful connection means local Kinesis is available
			isLocalKinesis = true;
		}
		catch (Exception e) {
			isLocalKinesis  = false;
		}
		assumeThat(isLocalKinesis).isTrue();
	}

	@Test
	void testKinesisToWebFlux() {
		this.kinesisTestConfiguration.eventQueue.offer("foo");
		this.kinesisTestConfiguration.eventQueue.offer("bar");
		this.kinesisTestConfiguration.eventQueue.offer("baz");

		Flux<String> seeFlux =
				this.webTestClient.get().uri("/sseFromKinesis")
						.exchange()
						.returnResult(String.class)
						.getResponseBody();

		StepVerifier
				.create(seeFlux)
				.expectNext("foo", "bar", "baz")
				.thenCancel()
				.verify();
	}

	@TestConfiguration
	@EnableAutoConfiguration(exclude =
			{ ContextResourceLoaderAutoConfiguration.class,
					ContextStackAutoConfiguration.class })
	public static class KinesisTestConfiguration {

		private static final int DEFAULT_KINESALITE_PORT = 4568;

		private BlockingQueue<String> eventQueue = new LinkedBlockingQueue<>();

		@Bean
		public Supplier<String> kinesisSource() {
			return () -> this.eventQueue.poll();
		}

		@Bean
		public AmazonKinesisAsync amazonKinesis() {
			// See https://github.com/mhart/kinesalite#cbor-protocol-issues-with-the-java-sdk
			System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");

			return AmazonKinesisAsyncClientBuilder.standard()
					.withClientConfiguration(
							new ClientConfiguration()
									.withMaxErrorRetry(0)
									.withConnectionTimeout(1000))
					.withEndpointConfiguration(
							new AwsClientBuilder.EndpointConfiguration("http://localhost:" + DEFAULT_KINESALITE_PORT,
									Regions.DEFAULT_REGION.getName()))
					.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("", "")))
					.build();
		}

		@Bean
		public LockRegistry lockRegistry() {
			return new DefaultLockRegistry();
		}

		@Bean
		public ConcurrentMetadataStore simpleMetadataStore() {
			return new SimpleMetadataStore();
		}


		@Bean
		public AmazonDynamoDBAsync dynamoDB() {
			return null;
		}

	}

}
