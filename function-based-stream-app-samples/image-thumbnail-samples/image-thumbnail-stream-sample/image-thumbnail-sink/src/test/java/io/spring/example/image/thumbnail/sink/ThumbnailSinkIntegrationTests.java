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

package io.spring.example.image.thumbnail.sink;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.support.MessageBuilder;

import static org.awaitility.Awaitility.await;

public class ThumbnailSinkIntegrationTests {

	@TempDir
	File tempDir;

	@Test
	void test() throws IOException {
		byte[] data = readAllBytes(new ClassPathResource("firetruck.jpg"));
		TestChannelBinderConfiguration.applicationContextRunner(ThumbnailSinkApplication.class)
				.withPropertyValues(applicationProperties())
				.withPropertyValues(
						"file.consumer.directory=" + tempDir.getAbsolutePath())
				.run(context -> {
					InputDestination inputDestination = context.getBean(InputDestination.class);
					inputDestination.send(MessageBuilder.withPayload(data).build());
				});
		await().timeout(Duration.ofSeconds(10))
				.until(() -> Files.exists(Paths.get(tempDir.getAbsolutePath(), "thumbnail-1.jpg")));
	}

	private byte[] readAllBytes(Resource resource) throws IOException {
		InputStream is = resource.getInputStream();
		byte[] bytes = new byte[is.available()];
		is.read(bytes);
		return bytes;
	}

	private String[] applicationProperties() throws IOException {
		Properties properties = new Properties();
		properties.load(new ClassPathResource("application.properties").getInputStream());
		ArrayList<String> props = new ArrayList(properties.size());
		properties.forEach((k, v) -> props.add(String.format("%s=%s", k.toString(), v.toString())));
		return props.toArray(new String[properties.size()]);
	}
}
