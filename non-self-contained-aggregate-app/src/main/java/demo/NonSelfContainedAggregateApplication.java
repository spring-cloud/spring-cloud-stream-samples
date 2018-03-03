/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo;

import config.processor.ProcessorApplication;
import config.source.SourceApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.aggregate.AggregateApplicationBuilder;

/**
 * @author Ilayaperumal Gopinathan
 */
@SpringBootApplication
public class NonSelfContainedAggregateApplication {

	public static void main(String[] args) {
		new AggregateApplicationBuilder(NonSelfContainedAggregateApplication.class)
				.from(SourceApplication.class).args("--fixedDelay=1000")
				.via(ProcessorApplication.class).namespace("a").run("--spring.cloud.stream.bindings.output.destination=processor-output");
	}
}
