/*
 * Copyright 2017 the original author or authors.
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

package demo.stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import demo.repository.OrderRepository;

/**
 *
 * @author Peter Oates
 *
 */
@EnableBinding(OrderProcessor.class)
public class OrderStreamConfiguration {

	private final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private OrderRepository orders;

	@StreamListener(OrderProcessor.INPUT)
	public void processOrder(Event event) {
		//log the order received
		if (!event.getOriginator().equals("KinesisProducer")) {
			logger.info("An order has been received " + event.toString());
			orders.save(event.getSubject());
		}
		else {
			logger.info("An order has been placed from this service " + event.toString());
		}
	}

}
