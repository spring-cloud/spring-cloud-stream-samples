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

package org.springframework.cloud.stream.testing.source;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

import java.util.concurrent.BlockingQueue;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
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
public class FooBarSourceTests {

	@Autowired
	private Source channels;

	@Autowired
	private MessageCollector collector;

	@Test
	public void testMessages() {
		BlockingQueue<Message<?>> messages = this.collector.forChannel(channels.output());

		assertThat(messages, receivesPayloadThat(is("foo")));
		assertThat(messages, receivesPayloadThat(is("bar")));
		assertThat(messages, receivesPayloadThat(is("foo")));
		assertThat(messages, receivesPayloadThat(is("bar")));
	}

}
