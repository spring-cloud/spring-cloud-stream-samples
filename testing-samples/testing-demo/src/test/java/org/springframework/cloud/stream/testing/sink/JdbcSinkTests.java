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

package org.springframework.cloud.stream.testing.sink;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

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
public class JdbcSinkTests {

	@Autowired
	private Sink channels;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@SpyBean(name = "jdbcHandler")
	private MessageHandler jdbcMessageHandler;

	@Test
	@SuppressWarnings("unchecked")
	public void testMessages() {
		AbstractMessageChannel input = (AbstractMessageChannel) this.channels.input();

		final AtomicReference<Message<?>> messageAtomicReference = new AtomicReference<>();

		ChannelInterceptorAdapter assertionInterceptor = new ChannelInterceptorAdapter() {

			@Override
			public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
				messageAtomicReference.set(message);
				super.afterSendCompletion(message, channel, sent, ex);
			}

		};

		input.addInterceptor(assertionInterceptor);

		input.send(new GenericMessage<>("foo"));

		input.removeInterceptor(assertionInterceptor);

		input.send(new GenericMessage<>("bar"));

		List<Map<String, Object>> data = this.jdbcTemplate.queryForList("SELECT * FROM foobar");

		assertThat(data.size()).isEqualTo(2);
		assertThat(data.get(0).get("value")).isEqualTo("foo");
		assertThat(data.get(1).get("value")).isEqualTo("bar");

		Message<?> message1 = messageAtomicReference.get();
		assertThat(message1).isNotNull();
		assertThat(message1).hasFieldOrPropertyWithValue("payload", "foo");

		ArgumentCaptor<Message<?>> messageArgumentCaptor =
				(ArgumentCaptor<Message<?>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Message.class);

		verify(this.jdbcMessageHandler, times(2)).handleMessage(messageArgumentCaptor.capture());

		Message<?> message = messageArgumentCaptor.getValue();
		assertThat(message).hasFieldOrPropertyWithValue("payload", "bar");
	}

}
