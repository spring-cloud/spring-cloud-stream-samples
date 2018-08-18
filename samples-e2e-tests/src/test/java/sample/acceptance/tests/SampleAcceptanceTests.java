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

import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.io.File;

import static org.junit.Assert.fail;

/**
 * Do not run these tests as part of an IDE build or individually.
 * These are acceptance tests for the spring cloud stream samples.
 * The recommended way to run these tests are using the runAcceptanceTests.sh script in this module.
 * More about running that script can be found in the README.
 *
 * @author Soby Chacko
 */
public class SampleAcceptanceTests extends AbstractSampleTests {

	private static final Logger logger = LoggerFactory.getLogger(SampleAcceptanceTests.class);

	private Process process;

	@After
	public void stopTheApp() {
		if (process != null) {
			process.destroyForcibly();
		}
	}

	@Test
	public void testJdbcSourceSampleKafka() throws Exception {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/jdbc-source-kafka-sample.jar");
		File file = Files.newTemporaryFile();
		logger.info("Output is redirected to " + file.getAbsolutePath());
		pb.redirectOutput(file);
		process = pb.start();

		waitForLogEntryInFile("JDBC Source", file,"Started SampleJdbcSource in");

		waitForLogEntryInFile("JDBC Source", file,
				"Data received...[{id=1, name=Bob, tag=null}, {id=2, name=Jane, tag=null}, {id=3, name=John, tag=null}]");
	}

	@Test
	public void testJdbcSourceSampleRabbit() throws Exception {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/jdbc-source-rabbit-sample.jar");
		File file = Files.newTemporaryFile();
		logger.info("Output is redirected to " + file.getAbsolutePath());
		pb.redirectOutput(file);
		process = pb.start();

		waitForLogEntryInFile("JDBC Source", file,"Started SampleJdbcSource in");

		waitForLogEntryInFile("JDBC Source", file,
				"Data received...[{id=1, name=Bob, tag=null}, {id=2, name=Jane, tag=null}, {id=3, name=John, tag=null}]");
	}

	@Test
	public void testJdbcSinkSampleKafka() throws Exception {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/jdbc-sink-kafka-sample.jar");
		File file = Files.newTemporaryFile();
		logger.info("Output is redirected to " + file.getAbsolutePath());
		pb.redirectOutput(file);
		process = pb.start();

		waitForLogEntryInFile("JDBC Sink", file,"Started SampleJdbcSink in");

		verifyJdbcSink();
	}

	@Test
	public void testJdbcSinkSampleRabbit() throws Exception {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/jdbc-sink-rabbit-sample.jar");
		File file = Files.newTemporaryFile();
		logger.info("Output is redirected to " + file.getAbsolutePath());
		pb.redirectOutput(file);
		process = pb.start();

		waitForLogEntryInFile("JDBC Sink", file,"Started SampleJdbcSink in");

		verifyJdbcSink();
	}

	@Test
	public void testDynamicSourceSampleKafka() throws Exception {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/dynamic-destination-source-kafka-sample.jar", "--management.endpoints.web.exposure.include=*");
		File file = Files.newTemporaryFile();
		logger.info("Output is redirected to " + file.getAbsolutePath());
		pb.redirectOutput(file);
		process = pb.start();

		waitForLogEntryInFile("Dynamic Source", file,"Started SourceApplication in");

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.postForObject(
				"http://localhost:8080",
				"{\"id\":\"customerId-1\",\"bill-pay\":\"100\"}", String.class);

		waitForLogEntryInFile("Dynamic Source", file,
				"Data received from customer-1...{\"id\":\"customerId-1\",\"bill-pay\":\"100\"}");

		restTemplate.postForObject(
				"http://localhost:8080",
				"{\"id\":\"customerId-2\",\"bill-pay2\":\"200\"}", String.class);

		waitForLogEntryInFile("Dynamic Source", file,
				"Data received from customer-2...{\"id\":\"customerId-2\",\"bill-pay2\":\"200\"}");

		Files.delete(file);
	}

	@Test
	public void testDynamicSourceSampleRabbit() throws Exception {

		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/dynamic-destination-source-rabbit-sample.jar", "--management.endpoints.web.exposure.include=*");
		File file = Files.newTemporaryFile();
		logger.info("Output is redirected to " + file.getAbsolutePath());
		pb.redirectOutput(file);
		process = pb.start();

		waitForLogEntryInFile("Dynamic Source", file,"Started SourceApplication in");

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.postForObject(
				"http://localhost:8080",
				"{\"id\":\"customerId-1\",\"bill-pay\":\"100\"}", String.class);

		waitForLogEntryInFile("Dynamic Source", file,
				"Data received from customer-1...{\"id\":\"customerId-1\",\"bill-pay\":\"100\"}");

		restTemplate.postForObject(
				"http://localhost:8080",
				"{\"id\":\"customerId-2\",\"bill-pay2\":\"200\"}", String.class);

		waitForLogEntryInFile("Dynamic Source", file,
				"Data received from customer-2...{\"id\":\"customerId-2\",\"bill-pay2\":\"200\"}");

		Files.delete(file);
	}

	@Test
	public void testMultiBinderKafkaInputRabbitOutput() throws Exception {

		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/multibinder-kafka-rabbit-sample.jar");
		File file = Files.newTemporaryFile();
		logger.info("Output is redirected to " + file.getAbsolutePath());
		pb.redirectOutput(file);
		process = pb.start();

		waitForLogEntryInFile("Multibinder", file,"Started MultibinderApplication in");

		waitForLogEntryInFile("Multibinder", file, "Data received...bar", "Data received...foo");
	}

	@Test
	public void testMultiBinderTwoKafkaClusters() throws Exception {

		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/multibinder-two-kafka-clusters-sample.jar",
				"--kafkaBroker1=localhost:9092", "--zk1=localhost:2181",
				"--kafkaBroker2=localhost:9093", "--zk2=localhost:2182");
		File file = Files.newTemporaryFile();
		logger.info("Output is redirected to " + file.getAbsolutePath());
		pb.redirectOutput(file);
		process = pb.start();

		waitForLogEntryInFile("Multibinder 2 Kafka Clusters", file,"Started MultibinderApplication in");

		waitForLogEntryInFile("Multibinder 2 Kafka Clusters", file, "Data received...bar", "Data received...foo");

		Files.delete(file);
	}

	@Test
	public void testStreamListenerBasicSampleKafka() throws Exception {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/streamlistener-basic-kafka-sample.jar");
		File file = Files.newTemporaryFile();
		logger.info("Output is redirected to " + file.getAbsolutePath());
		pb.redirectOutput(file);
		process = pb.start();

		waitForLogEntryInFile("Streamlistener basic", file,"Started TypeConversionApplication in");

		waitForLogEntryInFile("Streamlistener basic", file,
				"At the Source", "Sending value: {\"value\":\"hi\"}", "At the transformer",
				"Received value hi of type class demo.Bar",
				"Transforming the value to HI and with the type class demo.Bar",
				"At the Sink",
				"Received transformed message HI of type class demo.Foo");
	}

	@Test
	public void testStreamListenerBasicSampleRabbit() throws Exception {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/streamlistener-basic-rabbit-sample.jar");
		File file = Files.newTemporaryFile();
		logger.info("Output is redirected to " + file.getAbsolutePath());
		pb.redirectOutput(file);
		process = pb.start();

		waitForLogEntryInFile("Streamlistener basic", file,"Started TypeConversionApplication in");

		waitForLogEntryInFile("Streamlistener basic", file,
				"At the Source", "Sending value: {\"value\":\"hi\"}", "At the transformer",
				"Received value hi of type class demo.Bar",
				"Transforming the value to HI and with the type class demo.Bar",
				"At the Sink",
				"Received transformed message HI of type class demo.Foo");
	}

	@Test
	public void testReactiveProcessorSampleKafka() throws Exception {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/reactive-processor-kafka-sample.jar");
		File file = Files.newTemporaryFile();
		logger.info("Output is redirected to " + file.getAbsolutePath());
		pb.redirectOutput(file);
		process = pb.start();

		waitForLogEntryInFile("Reactive processor", file,"Started ReactiveProcessorApplication in");

		waitForLogEntryInFile("Reactive processor", file,
				"Data received: foobarfoobarfoo",
				"Data received: barfoobarfoobar");
	}

	@Test
	public void testReactiveProcessorSampleRabbit() throws Exception {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/reactive-processor-rabbit-sample.jar");
		File file = Files.newTemporaryFile();
		logger.info("Output is redirected to " + file.getAbsolutePath());
		pb.redirectOutput(file);
		process = pb.start();

		waitForLogEntryInFile("Reactive processor", file,"Started ReactiveProcessorApplication in");

		waitForLogEntryInFile("Reactive processor", file,
				"Data received: foobarfoobarfoo",
				"Data received: barfoobarfoobar");
	}

	@Test
	public void testSensorAverageReactiveSampleKafka() throws Exception {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/sensor-average-reactive-kafka-sample.jar");
		File file = Files.newTemporaryFile();
		logger.info("Output is redirected to " + file.getAbsolutePath());
		pb.redirectOutput(file);
		process = pb.start();

		waitForLogEntryInFile("Sensor average", file,"Started SensorAverageProcessorApplication in");

		waitForLogEntryInFile("Sensor average", file,
				"Data received: {\"id\":100100,\"average\":",
				"Data received: {\"id\":100200,\"average\":", "Data received: {\"id\":100300,\"average\":");
	}

	@Test
	public void testSensorAverageReactiveSampleRabbit() throws Exception {

		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/sensor-average-reactive-rabbit-sample.jar");
		File file = Files.newTemporaryFile();
		logger.info("Output is redirected to " + file.getAbsolutePath());
		pb.redirectOutput(file);
		process = pb.start();

		waitForLogEntryInFile("Sensor average", file,"Started SensorAverageProcessorApplication in");

		waitForLogEntryInFile("Sensor average", file,
				"Data received: {\"id\":100100,\"average\":",
				"Data received: {\"id\":100200,\"average\":", "Data received: {\"id\":100300,\"average\":");

		Files.delete(file);
	}

	@Test
	public void testKafkaStreamsWordCount() throws Exception {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/kafka-streams-word-count-sample.jar",
				"--spring.cloud.stream.kafka.streams.timeWindow.length=60000");
		File file = Files.newTemporaryFile();
		logger.info("Output is redirected to " + file.getAbsolutePath());
		pb.redirectOutput(file);
		process = pb.start();

		waitForLogEntryInFile("Kafka Streams WordCount", file,"Started KafkaStreamsWordCountApplication in");

		waitForLogEntryInFile("Kafka Streams WordCount", file,
				"Data received...{\"word\":\"foo\",\"count\":",
				"Data received...{\"word\":\"bar\",\"count\":",
				"Data received...{\"word\":\"foobar\",\"count\":",
				"Data received...{\"word\":\"baz\",\"count\":",
				"Data received...{\"word\":\"fox\",\"count\":");

		Files.delete(file);
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
}
