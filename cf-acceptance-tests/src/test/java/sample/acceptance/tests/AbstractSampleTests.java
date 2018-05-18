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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Stream;


/**
 * @author Soby Chacko
 */
abstract class AbstractSampleTests {

	private static final Logger logger = LoggerFactory.getLogger(AbstractSampleTests.class);

	boolean waitForLogEntry(String app, String route, String... entries) {
		logger.info("Looking for '" + StringUtils.arrayToCommaDelimitedString(entries) + "' in logfile for " + app + " - " + route);
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
				String log = getLog(route + "/actuator");
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

	private String getLog(String url) {
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


	protected boolean waitForLogEntryInFileWithoutFailing(String app, String route, String... entries) {
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
			String log = getLog(route + "/actuator");

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
