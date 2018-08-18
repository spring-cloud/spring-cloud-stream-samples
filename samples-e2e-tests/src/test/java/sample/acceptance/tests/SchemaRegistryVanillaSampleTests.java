package sample.acceptance.tests;

import org.assertj.core.util.Files;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

import static org.junit.Assert.fail;

/**
 * @author Soby Chacko
 */
public class SchemaRegistryVanillaSampleTests extends AbstractSampleTests {

	private static final Logger logger = LoggerFactory.getLogger(SchemaRegistryVanillaSampleTests.class);

	@Test
	public void testSchemaRegistryVanillaKafka() throws Exception {
		runAgainstMiddleware("/tmp/schema-registry-vanilla-registry-kafka.jar",
				"/tmp/schema-registry-vanilla-consumer-kafka.jar",
				"/tmp/schema-registry-vanilla-producer1-kafka.jar",
				"/tmp/schema-registry-vanilla-producer2-kafka.jar");
	}

	@Test
	public void testSchemaRegistryVanillaRabbit() throws Exception {
		runAgainstMiddleware("/tmp/schema-registry-vanilla-registry-rabbit.jar",
				"/tmp/schema-registry-vanilla-consumer-rabbit.jar",
				"/tmp/schema-registry-vanilla-producer1-rabbit.jar",
				"/tmp/schema-registry-vanilla-producer2-rabbit.jar");
	}

	private void runAgainstMiddleware(String registryJar, String consumerJar, String producer1Jar, String producer2Jar) throws Exception {

		Process registryProcess = null;
		Process consumerProcess = null;
		Process producer1Process = null;
		Process producer2Process = null;

		try {

			ProcessBuilder pbRegistry = new ProcessBuilder("java", "-jar", registryJar);
			File registryFile = Files.newTemporaryFile();
			logger.info("Output is redirected to " + registryFile.getAbsolutePath());
			pbRegistry.redirectOutput(registryFile);
			registryProcess = pbRegistry.start();

			waitForLogEntryInFile("Schema Registry Vanilla Server", registryFile, "Started RegistryApplication in");

			ProcessBuilder pbConsumer = new ProcessBuilder("java", "-jar", consumerJar);
			File consumerFile = Files.newTemporaryFile();
			logger.info("Output is redirected to " + consumerFile.getAbsolutePath());
			pbConsumer.redirectOutput(consumerFile);
			consumerProcess = pbConsumer.start();

			waitForLogEntryInFile("Schema Registry Vanilla Consumer", consumerFile, "Started ConsumerApplication in");

			ProcessBuilder pbProducer1 = new ProcessBuilder("java", "-jar", producer1Jar);
			File producer1File = Files.newTemporaryFile();
			logger.info("Output is redirected to " + producer1File.getAbsolutePath());
			pbProducer1.redirectOutput(producer1File);
			producer1Process = pbProducer1.start();

			waitForLogEntryInFile("Schema Registry Vanilla Producer1", producer1File, "Started Producer1Application in");

			ProcessBuilder pbProducer2 = new ProcessBuilder("java", "-jar", producer2Jar);
			File producer2File = Files.newTemporaryFile();
			logger.info("Output is redirected to " + producer2File.getAbsolutePath());
			pbProducer2.redirectOutput(producer2File);
			producer2Process = pbProducer2.start();

			waitForLogEntryInFile("Schema Registry Vanilla Producer2", producer2File, "Started Producer2Application in");

			RestTemplate restTemplate = new RestTemplate();

			MultiValueMap<String, Object> parametersMap = new LinkedMultiValueMap<>();
			parametersMap.add("id", "foobar");
			parametersMap.add("temperature", 30);
			parametersMap.add("acceleration", 10);
			parametersMap.add("velocity", 20);

			restTemplate.postForObject(
					"http://localhost:9009/messagesX", parametersMap, String.class);

			boolean found = waitForLogEntryInFile("Schema Registry Vanilla Consumer", consumerFile,
					"{\"id\": \"foobar-v1\", \"internalTemperature\": 30.0, \"externalTemperature\": 0.0, \"acceleration\": 10.0, \"velocity\": 20.0}");
			if (!found) {
				fail("Could not find the test data in the logs");
			}

			restTemplate.postForObject(
					"http://localhost:9010/messagesX", parametersMap, String.class);

			waitForLogEntryInFile("Schema Registry Vanilla Consumer", consumerFile,
					"{\"id\": \"foobar-v2\", \"internalTemperature\": 30.0, \"externalTemperature\": 0.0, \"acceleration\": 10.0, \"velocity\": 20.0}");

		} finally {
			if (registryProcess != null) {
				registryProcess.destroyForcibly();
			}
			if (consumerProcess != null) {
				consumerProcess.destroyForcibly();
			}
			if (producer1Process != null) {
				producer1Process.destroyForcibly();
			}
			if (producer2Process != null) {
				producer2Process.destroyForcibly();
			}
		}
	}
}
