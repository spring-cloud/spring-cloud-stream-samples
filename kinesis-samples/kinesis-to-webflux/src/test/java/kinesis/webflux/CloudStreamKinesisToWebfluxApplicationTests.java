/*
 * Copyright 2018 the original author or authors.
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

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextResourceLoaderAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.metadata.MetadataStore;
import org.springframework.integration.metadata.SimpleMetadataStore;
import org.springframework.integration.support.locks.DefaultLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {
				"spring.cloud.stream.bindings." + CloudStreamKinesisToWebfluxApplicationTests.TestSource.TO_KINESIS_OUTPUT + ".destination = SSE_DATA",
				"spring.cloud.stream.bindings." + CloudStreamKinesisToWebfluxApplicationTests.TestSource.TO_KINESIS_OUTPUT + ".producer.headerMode = none",
				"logging.level.org.springframework.integration=TRACE"
		}
)
@AutoConfigureWebTestClient
public class CloudStreamKinesisToWebfluxApplicationTests {

	@ClassRule
	public static LocalKinesisResource localKinesisResource = new LocalKinesisResource();

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private TestSource testSource;

	@Test
	public void testKinesisToWebFlux() {
		this.testSource.toKinesisOutput().send(new GenericMessage<>("foo"));
		this.testSource.toKinesisOutput().send(new GenericMessage<>("bar"));
		this.testSource.toKinesisOutput().send(new GenericMessage<>("baz"));

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
	@EnableBinding(TestSource.class)
	@EnableAutoConfiguration(exclude = ContextResourceLoaderAutoConfiguration.class)
	public static class KinesisTestConfiguration {

		public static final int DEFAULT_KINESALITE_PORT = 4568;

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
	}

	interface TestSource {

		String TO_KINESIS_OUTPUT = "toKinesisOutput";

		@Output(TO_KINESIS_OUTPUT)
		MessageChannel toKinesisOutput();

	}

}
