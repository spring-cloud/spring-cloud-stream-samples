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

package org.springframework.cloud.stream.testing.sink;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.metrics.KafkaMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.test.mock.MockIntegration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;

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
		KafkaMetricsAutoConfiguration.class })
@DirtiesContext
class JdbcSinkTests {

	@Autowired
	@Qualifier("jdbcConsumer-in-0")
	private AbstractMessageChannel input;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@SpyBean(name = "jdbcHandler")
	private MessageHandler jdbcMessageHandler;

	@Test
	void testMessages() {
		AtomicReference<Message<?>> messageAtomicReference = new AtomicReference<>();

		ChannelInterceptor assertionInterceptor =
				new ChannelInterceptor() {

					@Override
					public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent,
							Exception ex) {

						messageAtomicReference.set(message);
					}

				};

		this.input.addInterceptor(assertionInterceptor);

		this.input.send(new GenericMessage<>("odd"));

		this.input.removeInterceptor(assertionInterceptor);

		this.input.send(new GenericMessage<>("even"));

		List<Map<String, Object>> data = this.jdbcTemplate.queryForList("SELECT * FROM SINK");

		assertThat(data.size()).isEqualTo(2);
		assertThat(data.get(0).get("value")).isEqualTo("odd");
		assertThat(data.get(1).get("value")).isEqualTo("even");

		Message<?> message1 = messageAtomicReference.get();
		assertThat(message1).isNotNull();
		assertThat(message1).hasFieldOrPropertyWithValue("payload", "odd");

		ArgumentCaptor<Message<?>> messageArgumentCaptor = MockIntegration.messageArgumentCaptor();

		verify(this.jdbcMessageHandler, times(2)).handleMessage(messageArgumentCaptor.capture());

		Message<?> message = messageArgumentCaptor.getValue();
		assertThat(message).hasFieldOrPropertyWithValue("payload", "even");
	}

}
