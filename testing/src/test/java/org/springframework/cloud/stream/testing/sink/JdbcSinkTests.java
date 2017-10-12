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

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.jdbc.core.JdbcTemplate;
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
public class JdbcSinkTests {

	@Autowired
	private Sink channels;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	public void testMessages() {
		this.channels.input().send(new GenericMessage<>("foo"));
		this.channels.input().send(new GenericMessage<>("bar"));

		List<Map<String, Object>> data = this.jdbcTemplate.queryForList("SELECT * FROM foobar");
		assertThat(data.size()).isEqualTo(2);
		assertThat(data.get(0).get("value")).isEqualTo("foo");
		assertThat(data.get(1).get("value")).isEqualTo("bar");
	}

}
