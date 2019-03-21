/*
 * Copyright 2015 the original author or authors.
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

package demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.handler.annotation.SendTo;

/**
 * @author Ilayaperumal Gopinathan
 */
@EnableBinding(Processor.class)
public class SampleTransformer {

	private static final String TRANSFORMATION_VALUE = "HI";

	private final Log logger = LogFactory.getLog(getClass());

	@StreamListener(Processor.INPUT)
	@SendTo(Processor.OUTPUT)
	public Bar receive(Bar bar) {
		logger.info("******************\nAt the transformer\n******************");
		logger.info("Received value "+ bar.getValue() + " of type " + bar.getClass());
		logger.info("Transforming the value to " + TRANSFORMATION_VALUE + " and with the type " + bar.getClass());
		bar.setValue(TRANSFORMATION_VALUE);
		return bar;
	}
}
