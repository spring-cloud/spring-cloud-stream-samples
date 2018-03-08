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

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

/**
 * Do not run these tests as part of an IDE build or individually.
 * These are acceptance tests for the spring cloud stream samples.
 * The recommended way to run these tests are using the runAcceptanceTests.sh script in this module.
 * More about running that script can be found in the README.
 *
 * @author Soby Chacko
 */
public class SampleAcceptanceTests {

	private static final Logger logger = LoggerFactory.getLogger(SampleAcceptanceTests.class);

	private Process process;

	private Process startTheApp(String[] cmds) throws Exception {
		ProcessBuilder pb = new ProcessBuilder(cmds);
		process = pb.start();
		return process;
	}

	@After
	public void stopTheApp() {
		if (process != null) {
			process.destroyForcibly();
		}
	}

	private void waitForExpectedMessagesToAppearInTheLogs(String app, String... textToSearch) {
		boolean foundAssertionStrings = waitForLogEntry(app, textToSearch);
		if (!foundAssertionStrings) {
			fail("Did not find the text looking for after waiting for 30 seconds");
		}
	}

	private void waitForAppToStartFully(String app, String message) {
		boolean started = waitForLogEntry(app, message);
		if (!started) {
			fail("process didn't start in 30 seconds");
		}
	}

	@Test
	public void testJdbcSourceSampleKafka() throws Exception {
		process = startTheApp(new String[]{
				"java", "-jar", "/tmp/jdbc-source-kafka-sample.jar", "--logging.file=/tmp/foobar.log",
				"--management.endpoints.web.exposure.include=*"
		});
		waitForAppToStartFully("JDBC Source", "Started SampleJdbcSource in");
		waitForExpectedMessagesToAppearInTheLogs("JDBC Source",
				"Data received...[{id=1, name=Bob, tag=null}, {id=2, name=Jane, tag=null}, {id=3, name=John, tag=null}]");
	}

	@Test
	public void testJdbcSourceSampleRabbit() throws Exception {
		process = startTheApp(new String[]{
				"java", "-jar", "/tmp/jdbc-source-rabbit-sample.jar", "--logging.file=/tmp/foobar.log",
				"--management.endpoints.web.exposure.include=*"
		});
		waitForAppToStartFully("JDBC Source", "Started SampleJdbcSource in");
		waitForExpectedMessagesToAppearInTheLogs("JDBC Source",
				"Data received...[{id=1, name=Bob, tag=null}, {id=2, name=Jane, tag=null}, {id=3, name=John, tag=null}]");
	}

	@Test
	public void testJdbcSinkSampleKafka() throws Exception {

		process = startTheApp(new String[]{
				"java", "-jar", "/tmp/jdbc-sink-kafka-sample.jar", "--logging.file=/tmp/foobar.log",
				"--management.endpoints.web.exposure.include=*"
		});
		waitForAppToStartFully("JDBC Sink", "Started SampleJdbcSink in");

		verifyJdbcSink();
	}

	@Test
	public void testJdbcSinkSampleRabbit() throws Exception {

		process = startTheApp(new String[]{
				"java", "-jar", "/tmp/jdbc-sink-rabbit-sample.jar", "--logging.file=/tmp/foobar.log",
				"--management.endpoints.web.exposure.include=*"
		});
		waitForAppToStartFully("JDBC Sink", "Started SampleJdbcSink in");

		verifyJdbcSink();
	}

	private void verifyJdbcSink() {
		JdbcTemplate db;
		DataSource dataSource = new SingleConnectionDataSource("jdbc:mariadb://localhost:3306/sample_mysql_db",
				"root", "pwd", false);

		db = new JdbcTemplate(dataSource);

		long timeout = System.currentTimeMillis() + (30 * 1000);
		boolean exists = false;
		while (!exists && System.currentTimeMillis() < timeout) {
			try {
				Thread.sleep(5 * 1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException(e.getMessage(), e);
			}

			Integer count = db.queryForObject("select count(*) from test", Integer.class);

			if (count > 0) {
				exists = true;
			}
		}
		if (!exists) {
			fail("No records found in database!");
		}
	}

	@Test
	public void testDynamicSourceSampleKafka() throws Exception {

		process = startTheApp(new String[]{
				"java", "-jar", "/tmp/dynamic-destination-source-kafka-sample.jar", "--logging.file=/tmp/foobar.log",
				"--management.endpoints.web.exposure.include=*"
		});
		verifyDynamicSourceApp();
	}

	@Test
	public void testDynamicSourceSampleRabbit() throws Exception {
		process = startTheApp(new String[]{
				"java", "-jar", "/tmp/dynamic-destination-source-rabbit-sample.jar", "--logging.file=/tmp/foobar.log",
				"--management.endpoints.web.exposure.include=*"
		});
		verifyDynamicSourceApp();
	}

	private void verifyDynamicSourceApp() {
		waitForAppToStartFully("Dynamic Source", "Started SourceApplication in");
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.postForObject(
				"http://localhost:8080",
				"{\"id\":\"customerId-1\",\"bill-pay\":\"100\"}", String.class);

		waitForExpectedMessagesToAppearInTheLogs("Dynamic Source",
				"Data received from customer-1...{\"id\":\"customerId-1\",\"bill-pay\":\"100\"}");

		restTemplate.postForObject(
				"http://localhost:8080",
				"{\"id\":\"customerId-2\",\"bill-pay2\":\"200\"}", String.class);

		waitForExpectedMessagesToAppearInTheLogs("Dynamic Source",
				"Data received from customer-2...{\"id\":\"customerId-2\",\"bill-pay2\":\"200\"}");
	}

	@Test
	public void testMultiBinderKafkaInputRabbitOutput() throws Exception {
		startTheApp(new String[]{"java", "-jar", "/tmp/multibinder-kafka-rabbit-sample.jar", "--logging.file=/tmp/foobar.log",
				"--management.endpoints.web.exposure.include=*"});

		waitForAppToStartFully("Multibinder", "Started MultibinderApplication in");

		waitForExpectedMessagesToAppearInTheLogs("Multibinder", "Data received...bar", "Data received...foo");
	}

	@Test
	public void testMultiBinderTwoKafkaClusters() throws Exception {

		startTheApp(new String[]{"java", "-jar", "/tmp/multibinder-two-kafka-clusters-sample.jar", "--logging.file=/tmp/foobar.log",
				"--management.endpoints.web.exposure.include=*",
				"--kafkaBroker1=localhost:9092", "--zk1=localhost:2181",
				"--kafkaBroker2=localhost:9093", "--zk2=localhost:2182"});

		waitForAppToStartFully("Multibinder 2 Kafka Clusters", "Started MultibinderApplication in");

		waitForExpectedMessagesToAppearInTheLogs("Multibinder 2 Kafka Clusters", "Data received...bar", "Data received...foo");
	}

	@Test
	public void testStreamListenerBasicSampleKafka() throws Exception {
		process = startTheApp(new String[]{
				"java", "-jar", "/tmp/streamlistener-basic-kafka-sample.jar", "--logging.file=/tmp/foobar.log",
				"--management.endpoints.web.exposure.include=*"
		});
		waitForAppToStartFully("Streamlistener basic", "Started TypeConversionApplication in");
		waitForExpectedMessagesToAppearInTheLogs("Streamlistener basic",
				"At the Source", "Sending value: {\"value\":\"hi\"}", "At the transformer",
				"Received value hi of type class demo.Bar",
				"Transforming the value to HI and with the type class demo.Bar",
				"At the Sink",
				"Received transformed message HI of type class demo.Foo");
	}

	@Test
	public void testStreamListenerBasicSampleRabbit() throws Exception {
		process = startTheApp(new String[]{
				"java", "-jar", "/tmp/streamlistener-basic-rabbit-sample.jar", "--logging.file=/tmp/foobar.log",
				"--management.endpoints.web.exposure.include=*"
		});
		waitForAppToStartFully("Streamlistener basic", "Started TypeConversionApplication in");
		waitForExpectedMessagesToAppearInTheLogs("Streamlistener basic",
				"At the Source", "Sending value: {\"value\":\"hi\"}", "At the transformer",
				"Received value hi of type class demo.Bar",
				"Transforming the value to HI and with the type class demo.Bar",
				"At the Sink",
				"Received transformed message HI of type class demo.Foo");
	}

	@Test
	public void testReactiveProcessorSampleKafka() throws Exception {
		process = startTheApp(new String[]{
				"java", "-jar", "/tmp/reactive-processor-kafka-sample.jar", "--logging.file=/tmp/foobar.log",
				"--management.endpoints.web.exposure.include=*"
		});
		waitForAppToStartFully("Reactive processor", "Started ReactiveProcessorApplication in");
		waitForExpectedMessagesToAppearInTheLogs("Reactive processor",
				"Data received: foobarfoobarfoo",
				"Data received: barfoobarfoobar");
	}

	@Test
	public void testReactiveProcessorSampleRabbit() throws Exception {
		process = startTheApp(new String[]{
				"java", "-jar", "/tmp/reactive-processor-rabbit-sample.jar", "--logging.file=/tmp/foobar.log",
				"--management.endpoints.web.exposure.include=*"
		});
		waitForAppToStartFully("Reactive processor", "Started ReactiveProcessorApplication in");
		waitForExpectedMessagesToAppearInTheLogs("Reactive processor",
				"Data received: foobarfoobarfoo",
				"Data received: barfoobarfoobar");
	}

	@Test
	public void testSensorAverageReactiveSampleKafka() throws Exception {
		process = startTheApp(new String[]{
				"java", "-jar", "/tmp/sensor-average-reactive-kafka-sample.jar", "--logging.file=/tmp/foobar.log",
				"--management.endpoints.web.exposure.include=*"
		});
		waitForAppToStartFully("Sensor average", "Started SensorAverageProcessorApplication in");
		waitForExpectedMessagesToAppearInTheLogs("Sensor average",
				"Data received: {\"id\":100100,\"average\":",
				"Data received: {\"id\":100200,\"average\":", "Data received: {\"id\":100300,\"average\":");
	}

	@Test
	public void testSensorAverageReactiveSampleRabbit() throws Exception {
		process = startTheApp(new String[]{
				"java", "-jar", "/tmp/sensor-average-reactive-rabbit-sample.jar", "--logging.file=/tmp/foobar.log",
				"--management.endpoints.web.exposure.include=*"
		});
		waitForAppToStartFully("Sensor average", "Started SensorAverageProcessorApplication in");
		waitForExpectedMessagesToAppearInTheLogs("Sensor average",
				"Data received: {\"id\":100100,\"average\":",
				"Data received: {\"id\":100200,\"average\":", "Data received: {\"id\":100300,\"average\":");
	}

	@Test
	public void testKafkaStreamsWordCount() throws Exception {
		startTheApp(new String[]{"java", "-jar", "/tmp/kafka-streams-word-count-sample.jar", "--logging.file=/tmp/foobar.log",
				"--management.endpoints.web.exposure.include=*",
				"--spring.cloud.stream.kafka.streams.timeWindow.length=60000"});

		waitForAppToStartFully("Kafka Streams WordCount", "Started KafkaStreamsWordCountApplication in");

		waitForExpectedMessagesToAppearInTheLogs("Kafka Streams WordCount",
				"Data received...{\"word\":\"foo\",\"count\":1,",
				"Data received...{\"word\":\"bar\",\"count\":1,",
				"Data received...{\"word\":\"foobar\",\"count\":1,",
				"Data received...{\"word\":\"baz\",\"count\":1,",
				"Data received...{\"word\":\"fox\",\"count\":1,");
	}

	boolean waitForLogEntry(String app, String... entries) {
		logger.info("Looking for '" + StringUtils.arrayToCommaDelimitedString(entries) + "' in logfile for " + app);
		long timeout = System.currentTimeMillis() + (30 * 1000);
		boolean exists = false;
		while (!exists && System.currentTimeMillis() < timeout) {
			try {
				Thread.sleep(7 * 1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException(e.getMessage(), e);
			}
			if (!exists) {
				logger.info("Polling to get log file. Remaining poll time = "
						+ (timeout - System.currentTimeMillis() + " ms."));
				String log = getLog("http://localhost:8080/actuator");
				if (log != null) {
					if (Stream.of(entries).allMatch(s -> log.contains(s))) {
						exists = true;
					}
				}
			}
		}
		if (exists) {
			logger.info("Matched all '" + StringUtils.arrayToCommaDelimitedString(entries) + "' in logfile for app " + app);
		} else {
			logger.error("ERROR: Couldn't find all '" + StringUtils.arrayToCommaDelimitedString(entries) + "' in logfile for " + app);
		}
		return exists;
	}

	String getLog(String url) {
		RestTemplate restTemplate = new RestTemplate();
		String logFileUrl = String.format("%s/logfile", url);
		String log = null;
		try {
			log = restTemplate.getForObject(logFileUrl, String.class);
			if (log == null) {
				logger.info("Unable to retrieve logfile from '" + logFileUrl);
			} else {
				logger.info("Retrieved logfile from '" + logFileUrl);
			}
		} catch (HttpClientErrorException e) {
			logger.info("Failed to access logfile from '" + logFileUrl + "' due to : " + e.getMessage());
		} catch (Exception e) {
			logger.warn("Error while trying to access logfile from '" + logFileUrl + "' due to : " + e);
		}
		return log;
	}

}
