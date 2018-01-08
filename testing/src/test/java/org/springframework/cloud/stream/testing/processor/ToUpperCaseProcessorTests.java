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

package org.springframework.cloud.stream.testing.processor;

import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.BlockingQueue;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesMessageThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;
import static org.springframework.integration.test.matcher.PayloadAndHeaderMatcher.sameExceptIgnorableHeaders;

/**
 * The Spring Boot-base test-case to demonstrate how can we test Spring Cloud Stream applications
 * with available testing tools.
 *
 * @author Artem Bilan
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
@Ignore
public class ToUpperCaseProcessorTests {

	@Autowired
	private Processor channels;

	@Autowired
	private MessageCollector collector;

	@SpyBean
	private ToUpperCaseProcessor toUpperCaseProcessor;

	@Test
	@SuppressWarnings("unchecked")
	public void testMessages() {
		SubscribableChannel input = this.channels.input();

		input.send(new GenericMessage<>("foo"));
		input.send(new GenericMessage<>("bar"));
		input.send(new GenericMessage<>("foo meets bar"));
		input.send(new GenericMessage<>("nothing but the best test"));

		BlockingQueue<Message<?>> messages = this.collector.forChannel(channels.output());

		assertThat(messages, receivesPayloadThat(is("FOO")));
		assertThat(messages, receivesPayloadThat(is("BAR")));
		assertThat(messages, receivesPayloadThat(is("FOO MEETS BAR")));
		assertThat(messages, receivesPayloadThat(not("nothing but the best test")));

		Message<String> testMessage =
				MessageBuilder.withPayload("headers")
						.setHeader("foo", "bar")
						.build();

		input.send(testMessage);

		Message<String> expected =
				MessageBuilder.withPayload("HEADERS")
						.copyHeaders(testMessage.getHeaders())
						.build();

		Matcher<Message<Object>> sameExceptIgnorableHeaders =
				(Matcher<Message<Object>>) (Matcher<?>) sameExceptIgnorableHeaders(expected);

		assertThat(messages, receivesMessageThat(sameExceptIgnorableHeaders));

		verify(this.toUpperCaseProcessor, times(5)).transform(anyString());
	}

}
