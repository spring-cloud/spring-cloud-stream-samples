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

package org.springframework.cloud.stream.testing.source;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.metrics.KafkaMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;

/**
 * The Spring Boot-base test-case to demonstrate how can we test Spring Cloud Stream applications
 * with available testing tools.
 *
 * @author Artem Bilan
 *
 */
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.NONE,
		properties = "spring.cloud.stream.poller.fixed-delay=1")
@ImportAutoConfiguration(exclude = {
		KafkaAutoConfiguration.class,
		KafkaMetricsAutoConfiguration.class,
		DataSourceAutoConfiguration.class,
		TransactionAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class })
@DirtiesContext
class OddEvenSourceTests {

	@Autowired
	@Qualifier("oddEvenSupplier-out-0")
	MessageChannel outputDestination;

	@Autowired
	MessageCollector collector;

	@Test
	void testMessages() {
		BlockingQueue<Message<?>> messages = this.collector.forChannel(this.outputDestination);

		assertThat(messages, receivesPayloadThat(is("odd")));
		assertThat(messages, receivesPayloadThat(is("even")));
		assertThat(messages, receivesPayloadThat(is("odd")));
		assertThat(messages, receivesPayloadThat(is("even")));
	}

}
