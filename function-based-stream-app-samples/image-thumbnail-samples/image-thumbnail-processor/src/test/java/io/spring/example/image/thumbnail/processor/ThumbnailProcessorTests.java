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

package io.spring.example.image.thumbnail.processor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

public class ThumbnailProcessorTests {
	@TempDir
	File tempDir;

	@Test
	void test() throws IOException {
		Resource resource = new ClassPathResource("firetruck.jpg");
		byte[] data = readAllBytes(resource);
		byte[] thumbnail = new ThumbnailProcessor().apply(data);
		Path path = Paths.get(tempDir.getAbsolutePath(), "thumbnail.jpg");
		Files.write(path, thumbnail);
		assertThat(Files.exists(path)).isTrue();
	}

	private byte[] readAllBytes(Resource resource) throws IOException {
		InputStream is = resource.getInputStream();
		byte[] bytes = new byte[is.available()];
		is.read(bytes);
		return bytes;
	}
}
