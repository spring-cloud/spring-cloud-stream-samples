/*
 * Copyright 2015-2017 the original author or authors.
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

package multibinder;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaProducerProperties;
import org.springframework.cloud.stream.binder.rabbit.RabbitMessageChannelBinder;
import org.springframework.cloud.stream.binder.rabbit.properties.RabbitConsumerProperties;
import org.springframework.cloud.stream.binder.test.junit.rabbit.RabbitTestSupport;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

/**
 * @author Marius Bogoevici
 * @author Gary Russell
 */
@DirtiesContext
@Ignore
public class RabbitAndKafkaBinderApplicationTests {

	@ClassRule
	public static RabbitTestSupport rabbitTestSupport = new RabbitTestSupport();

	@ClassRule
	public static KafkaEmbedded kafkaEmbedded = new KafkaEmbedded(1, true, "test");

	private final String randomGroup = UUID.randomUUID().toString();

	@After
	public void cleanUp() {
		RabbitAdmin admin = new RabbitAdmin(rabbitTestSupport.getResource());
		admin.deleteQueue("binder.dataOut.default");
		admin.deleteQueue("binder.dataOut." + this.randomGroup);
		admin.deleteExchange("binder.dataOut");
	}

	@Test
	public void contextLoads() throws Exception {
		// passing connection arguments arguments to the embedded Kafka instance
		ConfigurableApplicationContext context = SpringApplication.run(MultibinderApplication.class,
				"--spring.cloud.stream.kafka.binder.brokers=" + kafkaEmbedded.getBrokersAsString(),
				"--spring.cloud.stream.kafka.binder.zkNodes=" + kafkaEmbedded.getZookeeperConnectionString());
		context.close();
	}

	@Test
	public void messagingWorks() throws Exception {
		// passing connection arguments arguments to the embedded Kafka instance
		ConfigurableApplicationContext context = SpringApplication.run(MultibinderApplication.class,
				"--spring.cloud.stream.kafka.binder.brokers=" + kafkaEmbedded.getBrokersAsString(),
				"--spring.cloud.stream.kafka.binder.zkNodes=" + kafkaEmbedded.getZookeeperConnectionString(),
				"--spring.cloud.stream.bindings.input.group=testGroup",
				"--spring.cloud.stream.bindings.output.producer.requiredGroups=" + this.randomGroup);
		DirectChannel dataProducer = new DirectChannel();
		BinderFactory binderFactory = context.getBean(BinderFactory.class);

		QueueChannel dataConsumer = new QueueChannel();

		((RabbitMessageChannelBinder) binderFactory.getBinder("rabbit", MessageChannel.class)).bindConsumer("dataOut", this.randomGroup,
				dataConsumer, new ExtendedConsumerProperties<>(new RabbitConsumerProperties()));

		((KafkaMessageChannelBinder) binderFactory.getBinder("kafka", MessageChannel.class))
				.bindProducer("dataIn", dataProducer, new ExtendedProducerProperties<>(new KafkaProducerProperties()));

		String testPayload = "testFoo" + this.randomGroup;
		dataProducer.send(MessageBuilder.withPayload(testPayload).build());

		Message<?> receive = dataConsumer.receive(60_000);
		Assert.assertThat(receive, Matchers.notNullValue());
		Assert.assertThat(receive.getPayload(), CoreMatchers.equalTo(testPayload));
		context.close();
	}

}
