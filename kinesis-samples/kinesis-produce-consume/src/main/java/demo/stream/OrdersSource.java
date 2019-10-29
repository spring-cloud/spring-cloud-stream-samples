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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


/**
 *
 * @author Peter Oates
 * @author Artem Bilan
 *
 */
@Component
public class OrdersSource {

	private final Log logger = LogFactory.getLog(getClass());

	private BlockingQueue<Event> orderEvent = new LinkedBlockingQueue<>();

	@Bean
	public Supplier<Event> produceOrder() {
		return () -> this.orderEvent.poll();
	}

	public void sendOrder(Event event) {
		this.orderEvent.offer(event);
		logger.info("Event sent: " + event.toString());
	}

}
