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

import java.util.List;

import org.springframework.cloud.stream.test.junit.AbstractExternalResourceTestSupport;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder;
import com.amazonaws.services.kinesis.model.ListStreamsRequest;
import com.amazonaws.services.kinesis.model.ListStreamsResult;
import com.amazonaws.services.kinesis.model.ResourceNotFoundException;

/**
 * An {@link AbstractExternalResourceTestSupport} implementation for Kinesis local service.
 *
 * @author Artem Bilan
 * @author Jacob Severson
 *
 */
public class LocalKinesisResource
		extends AbstractExternalResourceTestSupport<AmazonKinesisAsync> {

	/**
	 * The default port for the local Kinesis service.
	 */
	public static final int DEFAULT_PORT = 4568;

	private final int port;

	public LocalKinesisResource() {
		this(DEFAULT_PORT);
	}

	public LocalKinesisResource(int port) {
		super("KINESIS");
		this.port = port;
	}

	@Override
	protected void obtainResource() {
		// See https://github.com/mhart/kinesalite#cbor-protocol-issues-with-the-java-sdk
		System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY,
				"true");

		this.resource = AmazonKinesisAsyncClientBuilder.standard()
				.withClientConfiguration(new ClientConfiguration().withMaxErrorRetry(0)
						.withConnectionTimeout(1000))
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
						"http://localhost:" + this.port,
						Regions.DEFAULT_REGION.getName()))
				.withCredentials(
						new AWSStaticCredentialsProvider(new BasicAWSCredentials("", "")))
				.build();

		// Check connection
		this.resource.listStreams();
	}

	@Override
	protected void cleanupResource() {
		ListStreamsRequest listStreamsRequest = new ListStreamsRequest();
		ListStreamsResult listStreamsResult = this.resource
				.listStreams(listStreamsRequest);

		List<String> streamNames = listStreamsResult.getStreamNames();

		while (listStreamsResult.getHasMoreStreams()) {
			if (streamNames.size() > 0) {
				listStreamsRequest.setExclusiveStartStreamName(
						streamNames.get(streamNames.size() - 1));
			}
			listStreamsResult = this.resource.listStreams(listStreamsRequest);
			streamNames.addAll(listStreamsResult.getStreamNames());
		}

		for (String stream : streamNames) {
			this.resource.deleteStream(stream);
			while (true) {
				try {
					this.resource.describeStream(stream);
					try {
						Thread.sleep(100);
					}
					catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
						throw new IllegalStateException(ex);
					}
				}
				catch (ResourceNotFoundException ex) {
					break;
				}
			}
		}

		System.clearProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY);
		this.resource.shutdown();
	}

}
