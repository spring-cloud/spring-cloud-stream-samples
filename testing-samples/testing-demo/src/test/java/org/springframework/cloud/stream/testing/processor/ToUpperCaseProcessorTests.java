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

package org.springframework.cloud.stream.testing.processor;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesMessageThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;
import static org.springframework.integration.test.matcher.PayloadAndHeaderMatcher.sameExceptIgnorableHeaders;

import java.util.concurrent.BlockingQueue;

import org.hamcrest.Matcher;
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
import org.springframework.http.MediaType;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.MimeType;

/**
 * The Spring Boot-base test-case to demonstrate how can we test Spring Cloud Stream applications
 * with available testing tools.
 *
 * @author Artem Bilan
 *
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ImportAutoConfiguration(exclude = {
		KafkaAutoConfiguration.class,
		KafkaMetricsAutoConfiguration.class,
		DataSourceAutoConfiguration.class,
		TransactionAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class })
@DirtiesContext
class ToUpperCaseProcessorTests {

	@Autowired
	@Qualifier("uppercaseFunction-in-0")
	private MessageChannel input;

	@Autowired
	@Qualifier("uppercaseFunction-out-0")
	private MessageChannel output;

	@Autowired
	private MessageCollector collector;

	@Test
	@SuppressWarnings("unchecked")
	void testMessages() {
		this.input.send(new GenericMessage<>("odd"));
		this.input.send(new GenericMessage<>("even"));
		this.input.send(new GenericMessage<>("odd meets even"));
		this.input.send(new GenericMessage<>("nothing but the best test"));

		BlockingQueue<Message<?>> messages = this.collector.forChannel(this.output);

		assertThat(messages, receivesPayloadThat(is("ODD")));
		assertThat(messages, receivesPayloadThat(is("EVEN")));
		assertThat(messages, receivesPayloadThat(is("ODD MEETS EVEN")));
		assertThat(messages, receivesPayloadThat(not("nothing but the best test")));

		Message<String> testMessage =
				MessageBuilder.withPayload("headers")
						.setHeader("odd", "even")
						.build();

		input.send(testMessage);

		Message<String> expected =
				MessageBuilder.withPayload("HEADERS")
						.copyHeaders(testMessage.getHeaders())
						.setHeader(MessageHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
						.build();

		Matcher<Message<Object>> sameExceptIgnorableHeaders =
				(Matcher<Message<Object>>) (Matcher<?>) sameExceptIgnorableHeaders(expected, "accept");

		assertThat(messages, receivesMessageThat(sameExceptIgnorableHeaders));
	}

}
