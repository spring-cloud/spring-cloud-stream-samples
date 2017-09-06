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

package demo.stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;


/**
 *
 * @author Peter Oates
 *
 */
@Component
public class OrdersSource {

	private final Log logger = LogFactory.getLog(getClass());

	private OrderProcessor orderOut;

	@Autowired
	public OrdersSource(OrderProcessor orderOut) {
		this.orderOut = orderOut;
	}

	public void sendOrder(Event event) {
		orderOut.ordersOut().send(new GenericMessage<>(event));
		logger.info("Event sent: " + event.toString());
	}

}
