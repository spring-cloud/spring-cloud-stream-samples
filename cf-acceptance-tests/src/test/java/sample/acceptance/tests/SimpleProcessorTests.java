/*
 * Copyright 2018 the original author or authors.
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

package sample.acceptance.tests;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.fail;

/**
 * Do not run these tests as part of an IDE build or individually.
 * These are acceptance tests for the spring cloud stream samples.
 * The recommended way to run these tests are using the runAcceptanceTests.sh script in this module.
 * More about running that script can be found in the README.
 *
 * @author Soby Chacko
 */
public class SimpleProcessorTests extends AbstractSampleTests {

	private static final Logger logger = LoggerFactory.getLogger(SimpleProcessorTests.class);

	@Test
	public void testUppercaseTransformerRabbit() {

		String url = System.getProperty("uppercase.processor.route");

		boolean foundLogs = waitForLogEntry("Uppercase Transformer", url, "Started UppercaseTransformerApplication in",
				"Data received: FOO", "Data received: BAR");
		if(!foundLogs) {
			fail("Did not find the logging messages.");
		}
	}
}
