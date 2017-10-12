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


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

import java.util.concurrent.BlockingQueue;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Artem Bilan
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
public class ToUpperCaseProcessorTests {

	@Autowired
	private Processor channels;

	@Autowired
	private MessageCollector collector;

	@SpyBean
	private ToUpperCaseProcessor toUpperCaseProcessor;

	@Test
	public void testMessages() {
		this.channels.input().send(new GenericMessage<>("foo"));
		this.channels.input().send(new GenericMessage<>("bar"));
		this.channels.input().send(new GenericMessage<>("foo meets bar"));
		this.channels.input().send(new GenericMessage<>("nothing but the best test"));

		BlockingQueue<Message<?>> messages = this.collector.forChannel(channels.output());

		assertThat(messages, receivesPayloadThat(is("FOO")));
		assertThat(messages, receivesPayloadThat(is("BAR")));
		assertThat(messages, receivesPayloadThat(is("FOO MEETS BAR")));
		assertThat(messages, receivesPayloadThat(not("nothing but the best test")));

		verify(this.toUpperCaseProcessor, times(4)).transform(anyString());
	}

}
