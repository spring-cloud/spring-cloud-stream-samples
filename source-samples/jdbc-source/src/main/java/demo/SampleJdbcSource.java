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

package demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowBuilder;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.scheduling.support.PeriodicTrigger;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Soby Chacko
 */
@EnableBinding(Source.class)
@SpringBootApplication
@EnableConfigurationProperties({JdbcSourceProperties.class})
public class SampleJdbcSource {

	public static void main(String... args){
		SpringApplication.run(SampleJdbcSource.class, args);
	}

	@Autowired
	private JdbcSourceProperties properties;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private Source source;

	@Bean
	public MessageSource<Object> jdbcMessageSource() {
		JdbcPollingChannelAdapter jdbcPollingChannelAdapter =
				new JdbcPollingChannelAdapter(this.dataSource, this.properties.getQuery());
		jdbcPollingChannelAdapter.setUpdateSql(this.properties.getUpdate());
		return jdbcPollingChannelAdapter;
	}

	@Bean
	public IntegrationFlow pollingFlow() {
		IntegrationFlowBuilder flowBuilder = IntegrationFlows.from(jdbcMessageSource());
		flowBuilder.channel(this.source.output());
		return flowBuilder.get();
	}

	@Bean(
			name = {"defaultPoller", "org.springframework.integration.context.defaultPollerMetadata"}
	)
	public PollerMetadata defaultPoller() {
		PollerMetadata pollerMetadata = new PollerMetadata();
		PeriodicTrigger trigger = new PeriodicTrigger(this.properties.getTriggerDelay(), TimeUnit.SECONDS);
		pollerMetadata.setTrigger(trigger);
		pollerMetadata.setMaxMessagesPerPoll(1L);
		return pollerMetadata;
	}

	//Following method populates the database with some test data
	@PostConstruct
	public void initializeData(){
			ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
			populator.addScript(resourceLoader.getResource("classpath:sample-schema.sql"));
			populator.setContinueOnError(true);
			DatabasePopulatorUtils.execute(populator, dataSource);
	}

	//Following sink is used as a test consumer. It logs the data received through the consumer.
	@EnableBinding(Sink.class)
	static class TestSink {

		private final Log logger = LogFactory.getLog(getClass());

		@StreamListener(Sink.INPUT)
		public void receive(List<Object> list) {
			logger.info("Data received..." + list);
			//System.out.println("Data received..." + list);
		}
	}

}
