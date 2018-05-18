package sample.acceptance.tests;

import org.assertj.core.util.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

/**
 * @author Soby Chacko
 */
public abstract class AbstractSampleTests {

	private static final Logger logger = LoggerFactory.getLogger(AbstractSampleTests.class);

	protected boolean waitForLogEntryInFile(String app, File f, String... entries) {
		logger.info("Looking for '" + StringUtils.arrayToCommaDelimitedString(entries) + "' in logfile for " + app);
		long timeout = System.currentTimeMillis() + (60 * 1000);
		boolean exists = false;
		while (!exists && System.currentTimeMillis() < timeout) {
			try {
				Thread.sleep(2 * 1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException(e.getMessage(), e);
			}
			logger.info("Polling to get log file. Remaining poll time = "
					+ (timeout - System.currentTimeMillis() + " ms."));
			String log = Files.contentOf(f, StandardCharsets.UTF_8);

			if (log != null) {
				if (Stream.of(entries).allMatch(log::contains)) {
					exists = true;
				}
			}
		}
		if (exists) {
			logger.info("Matched all '" + StringUtils.arrayToCommaDelimitedString(entries) + "' in logfile for app " + app);
		} else {
			logger.error("ERROR: Couldn't find all '" + StringUtils.arrayToCommaDelimitedString(entries) + "' in logfile for " + app);
			fail("Could not find the test data in the logs");
		}
		return true;
	}

	protected boolean waitForLogEntryInFileWithoutFailing(String app, File f, String... entries) {
		logger.info("Looking for '" + StringUtils.arrayToCommaDelimitedString(entries) + "' in logfile for " + app);
		long timeout = System.currentTimeMillis() + (60 * 1000);
		boolean exists = false;
		while (!exists && System.currentTimeMillis() < timeout) {
			try {
				Thread.sleep(2 * 1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException(e.getMessage(), e);
			}
			logger.info("Polling to get log file. Remaining poll time = "
					+ (timeout - System.currentTimeMillis() + " ms."));
			String log = Files.contentOf(f, StandardCharsets.UTF_8);

			if (log != null) {
				if (Stream.of(entries).allMatch(log::contains)) {
					exists = true;
				}
			}
		}
		if (exists) {
			logger.info("Matched all '" + StringUtils.arrayToCommaDelimitedString(entries) + "' in logfile for app " + app);
		}
		return exists;
	}

}
