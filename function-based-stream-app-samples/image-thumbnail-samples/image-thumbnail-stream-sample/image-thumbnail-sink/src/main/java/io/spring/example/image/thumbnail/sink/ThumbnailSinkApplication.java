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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import io.spring.example.image.thumbnail.processor.ThumbnailProcessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.fn.consumer.file.FileConsumerConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@SpringBootApplication
@Import(FileConsumerConfiguration.class)
public class ThumbnailSinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThumbnailSinkApplication.class, args);
	}

	@Bean
	public Function<?, Message<?>> filenameEnricher() {
		AtomicInteger count = new AtomicInteger();
		return payload -> MessageBuilder.withPayload(payload).setHeader("filename",
				String.format("thumbnail-%d.jpg", count.incrementAndGet())).build();
	}

	@Bean
	ThumbnailProcessor thumbnailProcessor() {
		return new ThumbnailProcessor();
	}
}
