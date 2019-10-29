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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.kinesis.model.Record;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

/**
 * @author Artem Bilan
 */
@SpringBootApplication
@RestController
public class CloudStreamKinesisToWebfluxApplication {

	private final EmitterProcessor<String> recordProcessor = EmitterProcessor.create();

	@GetMapping(value = "/sseFromKinesis", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> getSeeFromKinesis() {
		return this.recordProcessor;
	}

	@Bean
	public Consumer<Flux<List<Record>>> kinesisSink() {
		return recordFlux ->
				recordFlux
						.flatMap(Flux::fromIterable)
						.map(record -> new String(record.getData().array(), StandardCharsets.UTF_8))
						.doOnNext(this.recordProcessor::onNext)
						.subscribe();
	}

	public static void main(String[] args) {
		SpringApplication.run(CloudStreamKinesisToWebfluxApplication.class, args);
	}

}
