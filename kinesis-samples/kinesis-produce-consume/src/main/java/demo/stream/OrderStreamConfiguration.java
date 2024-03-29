/*
 * Copyright 2017-2019 the original author or authors.
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

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Bean;

import demo.repository.OrderRepository;
import org.springframework.stereotype.Component;

/**
 *
 * @author Peter Oates
 * @author Artem Bilan
 *
 */
@Component
public class OrderStreamConfiguration {

	private final Log logger = LogFactory.getLog(getClass());

	@Bean
	public Consumer<Event> processOrder(OrderRepository orders) {
		return event -> {
			//log the order received
			if (!event.getOriginator().equals("KinesisProducer")) {
				logger.info("An order has been received " + event.toString());
				orders.save(event.getSubject());
			}
			else {
				logger.info("An order has been placed from this service " + event.toString());
			}
		};
	}

	@Bean
	public Consumer<List<Event>> processOrders(OrderRepository orders) {
		return eventList -> {
			//log the number of orders received and each order
			logger.info("Received " + eventList.size() + " orders");
			for(Event event: eventList) {
				if (!event.getOriginator().equals("KinesisProducer")) {
					logger.info("An order has been received " + event.toString());
				}
				else {
					logger.info("An order has been placed from this service " + event.toString());
				}
			}
		};
	}

}
