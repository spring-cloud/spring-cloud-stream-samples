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

package io.spring.example.image.thumbnail.standalone;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import io.spring.example.image.thumbnail.processor.ThumbnailProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.fn.http.request.HttpRequestFunctionConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.cloud.fn.http.request.HttpRequestFunctionConfiguration.HttpRequestFunction;

@SpringBootApplication
@Controller
@Import(HttpRequestFunctionConfiguration.class)
public class ThumbnailStandaloneApplication {
	private static Logger logger = LoggerFactory.getLogger(ThumbnailStandaloneApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ThumbnailStandaloneApplication.class, args);
	}

	private ThumbnailProcessor thumbnailProcessor = new ThumbnailProcessor();

	@Autowired
	private HttpRequestFunction httpRequestFunction;

	@Bean
	RouterFunction<?> routes() {
		return RouterFunctions.route()
				.GET("/thumbnail", this::createThumbnail)
				.build();
	}

	private Mono<ServerResponse> createThumbnail(ServerRequest serverRequest) {
		String url = serverRequest.queryParam("url").orElseThrow(() -> new RuntimeException("URL required"));

		return Mono.from(httpRequestFunction.apply(Flux.just(new GenericMessage<>(url)))
				.flatMap(image -> {
					Map<String, Object> model = new HashMap<>();
					byte[] thumbnail = thumbnailProcessor.apply((byte[]) image);
					logger.info("creating thumbnail for {}", url);
					model.put("url", url);
					model.put("thumb", new String(Base64.getEncoder().encode(thumbnail)));
					Mono<ServerResponse> serverResponse = ServerResponse.ok()
							.render("thumbnail", model);
					return serverResponse;
				}));
	}
}
