package sample.acceptance.tests;

import org.assertj.core.util.Files;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.fail;

/**
 * @author Soby Chacko
 */
public class PartitioningAcceptanceTests extends AbstractSampleTests {

	private static final Logger logger = LoggerFactory.getLogger(PartitioningAcceptanceTests.class);

	@Test
	public void testPartitioningKafka() throws Exception {
		Process producerProcess = null;
		Process consumer1Process = null;
		Process consumer2Process = null;

		try {
			ProcessBuilder producerProcessBuilder = new ProcessBuilder("java", "-jar", "/tmp/partitioning-producer-kafka.jar");
			File producerFile = Files.newTemporaryFile();
			logger.info("Output is redirected to " + producerFile.getAbsolutePath());
			producerProcessBuilder.redirectOutput(producerFile);
			producerProcess = producerProcessBuilder.start();

			waitForLogEntryInFile("Partitioning producer", producerFile, "Started PartProducerApplication in");

			ProcessBuilder consumer1Builder = new ProcessBuilder("java", "-jar", "/tmp/partitioning-consumer-kafka.jar", "--server.port=12001");
			File consumer1File = Files.newTemporaryFile();
			logger.info("Output is redirected to " + consumer1File.getAbsolutePath());
			consumer1Builder.redirectOutput(consumer1File);
			consumer1Process = consumer1Builder.start();

			ProcessBuilder consumer2Builder = new ProcessBuilder("java", "-jar", "/tmp/partitioning-consumer-kafka.jar", "--server.port=12002");
			File consumer2File = Files.newTemporaryFile();
			logger.info("Output is redirected to " + consumer2File.getAbsolutePath());
			consumer2Builder.redirectOutput(consumer2File);
			consumer2Process = consumer2Builder.start();

			Future<?> future1 = verifyPartitions("Partitioning Consumer-1", consumer1File, "Partitioning Consumer-2", consumer2File,
					"f received from partition 0", "g received from partition 0", "h received from partition 0");
			Future<?> future2 = verifyPartitions("Partitioning Consumer-1", consumer1File, "Partitioning Consumer-2", consumer2File,
					"fo received from partition 1", "go received from partition 1", "ho received from partition 1");
			Future<?> future3 = verifyPartitions("Partitioning Consumer-2",consumer2File, "Partitioning Consumer-1", consumer1File,
					"foo received from partition 2", "goo received from partition 2", "hoo received from partition 2");
			Future<?> future4 = verifyPartitions("Partitioning Consumer-2",consumer2File, "Partitioning Consumer-1", consumer1File,
					"fooz received from partition 3", "gooz received from partition 3", "hooz received from partition 3");

			verifyResults(future1, future2, future3, future4);
		}
		finally {
			if (producerProcess != null) {
				producerProcess.destroyForcibly();
			}
			if (consumer1Process != null) {
				consumer1Process.destroyForcibly();
			}
			if (consumer2Process != null) {
				consumer2Process.destroyForcibly();
			}
		}
	}

	@Test
	public void testPartitioningRabbit() throws Exception {
		Process producerProcess = null;
		Process consumer1Process = null;
		Process consumer2Process = null;
		Process consumer3Process = null;
		Process consumer4Process = null;

		try {
			ProcessBuilder producerProcessBuilder = new ProcessBuilder("java", "-jar", "/tmp/partitioning-producer-rabbit.jar");
			File producerFile = Files.newTemporaryFile();
			logger.info("Output is redirected to " + producerFile.getAbsolutePath());
			producerProcessBuilder.redirectOutput(producerFile);
			producerProcess = producerProcessBuilder.start();

			waitForLogEntryInFile("Partitioning producer", producerFile, "Started PartProducerApplication in");

			ProcessBuilder consumer1Builder = new ProcessBuilder("java", "-jar", "/tmp/partitioning-consumer-rabbit.jar", "--server.port=12003");
			File consumer1File = Files.newTemporaryFile();
			logger.info("Output is redirected to " + consumer1File.getAbsolutePath());
			consumer1Builder.redirectOutput(consumer1File);
			consumer1Process = consumer1Builder.start();

			ProcessBuilder consumer2Builder = new ProcessBuilder("java", "-jar", "/tmp/partitioning-consumer-rabbit.jar", "--server.port=12004",
					"--spring.cloud.stream.bindings.input.consumer.instanceIndex=1");
			File consumer2File = Files.newTemporaryFile();
			logger.info("Output is redirected to " + consumer2File.getAbsolutePath());
			consumer2Builder.redirectOutput(consumer2File);
			consumer2Process = consumer2Builder.start();

			ProcessBuilder consumer3Builder = new ProcessBuilder("java", "-jar", "/tmp/partitioning-consumer-rabbit.jar", "--server.port=12005",
					"--spring.cloud.stream.bindings.input.consumer.instanceIndex=2");
			File consumer3File = Files.newTemporaryFile();
			logger.info("Output is redirected to " + consumer3File.getAbsolutePath());
			consumer3Builder.redirectOutput(consumer3File);
			consumer3Process = consumer3Builder.start();

			ProcessBuilder consumer4Builder = new ProcessBuilder("java", "-jar", "/tmp/partitioning-consumer-rabbit.jar", "--server.port=12006",
					"--spring.cloud.stream.bindings.input.consumer.instanceIndex=3");
			File consumer4File = Files.newTemporaryFile();
			logger.info("Output is redirected to " + consumer4File.getAbsolutePath());
			consumer4Builder.redirectOutput(consumer4File);
			consumer4Process = consumer4Builder.start();

			Future<?> future1 = verifyPartitions("Partitioning Consumer-1", consumer1File,
					"f received from partition partitioned.destination.myGroup-0",
					"g received from partition partitioned.destination.myGroup-0",
					"h received from partition partitioned.destination.myGroup-0");
			Future<?> future2 = verifyPartitions("Partitioning Consumer-2", consumer2File,
					"fo received from partition partitioned.destination.myGroup-1",
					"go received from partition partitioned.destination.myGroup-1",
					"ho received from partition partitioned.destination.myGroup-1");
			Future<?> future3 = verifyPartitions("Partitioning Consumer-3",consumer3File,
					"foo received from partition partitioned.destination.myGroup-2",
					"goo received from partition partitioned.destination.myGroup-2",
					"hoo received from partition partitioned.destination.myGroup-2");
			Future<?> future4 = verifyPartitions("Partitioning Consumer-4",consumer4File,
					"fooz received from partition partitioned.destination.myGroup-3",
					"gooz received from partition partitioned.destination.myGroup-3",
					"hooz received from partition partitioned.destination.myGroup-3");

			verifyResults(future1, future2, future3, future4);
		}
		finally {
			if (producerProcess != null) {
				producerProcess.destroyForcibly();
			}
			if (consumer1Process != null) {
				consumer1Process.destroyForcibly();
			}
			if (consumer2Process != null) {
				consumer2Process.destroyForcibly();
			}
			if (consumer3Process != null) {
				consumer3Process.destroyForcibly();
			}
			if (consumer4Process != null) {
				consumer4Process.destroyForcibly();
			}
		}
	}

	private Future<?> verifyPartitions(String consumer1Msg, File consumer1File,
									   String consumer2Msg, File consumer2File,
									   String... entries) {

		ExecutorService executorService = Executors.newSingleThreadExecutor();

		Future<?> submit = executorService.submit(() -> {
			boolean found = waitForLogEntryInFileWithoutFailing(consumer1Msg, consumer1File, entries);
			if (!found) {
				found = waitForLogEntryInFileWithoutFailing(consumer2Msg, consumer2File, entries);
			}
			if (!found) {
				fail("Could not find the test data in the logs");
			}
		});

		executorService.shutdown();
		return submit;
	}

	private Future<?> verifyPartitions(String consumer1Msg, File consumer1File,
									   String... entries) {

		ExecutorService executorService = Executors.newSingleThreadExecutor();

		Future<?> submit = executorService.submit(() -> {
			boolean found = waitForLogEntryInFileWithoutFailing(consumer1Msg, consumer1File, entries);
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
