/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaBinderConfigurationProperties;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaConsumerProperties;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
public class TwoKafkaBindersApplicationTest {

	@ClassRule
	public static EmbeddedKafkaRule kafkaTestSupport1 = new EmbeddedKafkaRule(1, true, "input", "output");

	@ClassRule
	public static EmbeddedKafkaRule kafkaTestSupport2 = new EmbeddedKafkaRule(1, true, "input", "output");


	@BeforeClass
	public static void setupEnvironment() {
		System.setProperty("kafkaBroker1", kafkaTestSupport1.getEmbeddedKafka().getBrokersAsString());
		System.setProperty("kafkaBroker2", kafkaTestSupport2.getEmbeddedKafka().getBrokersAsString());
	}

	@Autowired
	private BinderFactory binderFactory;

	@Test
	public void contextLoads() {
		Binder<MessageChannel, ?, ?> binder1 = binderFactory.getBinder("kafka1", MessageChannel.class);
		KafkaMessageChannelBinder kafka1 = (KafkaMessageChannelBinder) binder1;
		DirectFieldAccessor directFieldAccessor1 = new DirectFieldAccessor(kafka1);
		KafkaBinderConfigurationProperties configuration1 =
				(KafkaBinderConfigurationProperties) directFieldAccessor1.getPropertyValue("configurationProperties");
		Assert.assertThat(configuration1.getBrokers(), arrayWithSize(1));
		Assert.assertThat(configuration1.getBrokers()[0], equalTo(kafkaTestSupport1.getEmbeddedKafka().getBrokersAsString()));

		Binder<MessageChannel, ?, ?> binder2 = binderFactory.getBinder("kafka2", MessageChannel.class);
		KafkaMessageChannelBinder kafka2 = (KafkaMessageChannelBinder) binder2;
		DirectFieldAccessor directFieldAccessor2 = new DirectFieldAccessor(kafka2);
		KafkaBinderConfigurationProperties configuration2 =
				(KafkaBinderConfigurationProperties) directFieldAccessor2.getPropertyValue("configurationProperties");
		Assert.assertThat(configuration2.getBrokers(), arrayWithSize(1));
		Assert.assertThat(configuration2.getBrokers()[0], equalTo(kafkaTestSupport2.getEmbeddedKafka().getBrokersAsString()));
	}

	@Test
	public void messagingWorks() {
		QueueChannel dataConsumer = new QueueChannel();
		((KafkaMessageChannelBinder) binderFactory.getBinder("kafka2", MessageChannel.class)).bindConsumer("dataOut", UUID.randomUUID().toString(),
				dataConsumer, new ExtendedConsumerProperties<>(new KafkaConsumerProperties()));

		//receiving test message sent by the test producer in the application
		Message<?> receive = dataConsumer.receive(60_000);
		Assert.assertThat(receive, Matchers.notNullValue());
		Assert.assertThat(receive.getPayload(), CoreMatchers.anyOf(equalTo("FOO".getBytes()), equalTo("BAR".getBytes())));
	}

}
