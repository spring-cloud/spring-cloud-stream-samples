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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.fail;

/**
 * Do not run these tests as part of an IDE build or individually.
 * These are acceptance tests for the spring cloud stream samples.
 * The recommended way to run these tests are using the runAcceptanceTests.sh script in this module.
 * More about running that script can be found in the README.
 *
 * @author Soby Chacko
 */
public class PartitionAcceptanceTests extends AbstractSampleTests {

	private static final Logger logger = LoggerFactory.getLogger(PartitionAcceptanceTests.class);

	@Test
	public void testPartitioningWith4ConsumersRabbit() throws Exception {

		Thread.sleep(10_000);

		String prodUrl = System.getProperty("partitioning.producer.route");

		boolean foundLogs = waitForLogEntry("Partitioning producer", prodUrl, "Started PartProducerApplication in");
		if(!foundLogs) {
			fail("Did not find the logging messages.");
		}

		String consumer1Url = System.getProperty("partitioning.consumer1.route");
		String consumer2Url = System.getProperty("partitioning.consumer2.route");
		String consumer3Url = System.getProperty("partitioning.consumer3.route");
		String consumer4Url = System.getProperty("partitioning.consumer4.route");

		Future<?> future1 = verifyPartitions("Partitioning Consumer-1", consumer1Url,
				"f received from partition partitioned.destination.myGroup-0",
				"g received from partition partitioned.destination.myGroup-0",
				"h received from partition partitioned.destination.myGroup-0");
		Future<?> future2 = verifyPartitions("Partitioning Consumer-2", consumer2Url,
				"fo received from partition partitioned.destination.myGroup-1",
				"go received from partition partitioned.destination.myGroup-1",
				"ho received from partition partitioned.destination.myGroup-1");
		Future<?> future3 = verifyPartitions("Partitioning Consumer-3",consumer3Url,
				"foo received from partition partitioned.destination.myGroup-2",
				"goo received from partition partitioned.destination.myGroup-2",
				"hoo received from partition partitioned.destination.myGroup-2");
		Future<?> future4 = verifyPartitions("Partitioning Consumer-4",consumer4Url,
				"fooz received from partition partitioned.destination.myGroup-3",
				"gooz received from partition partitioned.destination.myGroup-3",
				"hooz received from partition partitioned.destination.myGroup-3");

		verifyResults(future1, future2, future3, future4);
	}

	private Future<?> verifyPartitions(String consumer1Msg, String consumerRoute,
									   String... entries) {

		ExecutorService executorService = Executors.newSingleThreadExecutor();

		Future<?> submit = executorService.submit(() -> {
			boolean found = waitForLogEntryInFileWithoutFailing(consumer1Msg, consumerRoute, entries);
			if (!found) {
				fail("Could not find the test data in the logs");
			}
		});

		executorService.shutdown();
		return submit;
	}

	private void verifyResults(Future<?>... futures) throws Exception {
		for (Future<?> future : futures) {
			try {
				future.get();
			}
			catch (Exception e) {
				throw e;
			}
		}
	}
}
