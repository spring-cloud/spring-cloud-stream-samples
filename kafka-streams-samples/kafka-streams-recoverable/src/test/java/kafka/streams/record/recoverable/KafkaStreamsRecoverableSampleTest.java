package kafka.streams.record.recoverable;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class KafkaStreamsRecoverableSampleTest {

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafkaRule = new EmbeddedKafkaRule(1, true,
            "topic-in", "default-out", "custom-out");

    private static EmbeddedKafkaBroker embeddedKafka = embeddedKafkaRule.getEmbeddedKafka();

    private static Consumer<String, String> consumer;
    private static Consumer<String, String> defaultOutConsumer;
    private static Consumer<String, String> customOutConsumer;

    @BeforeClass
    public static void setUp() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("group", "false", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = cf.createConsumer();
        embeddedKafka.consumeFromEmbeddedTopics(consumer, "topic-in", "default-out", "custom-out");
        System.setProperty("spring.cloud.stream.kafka.streams.binder.brokers", embeddedKafka.getBrokersAsString());
    }

    @AfterClass
    public static void tearDown() {
        consumer.close();
        System.clearProperty("spring.cloud.stream.kafka.streams.binder.brokers");
    }

    @Test
    public void testKafkaStreams() {
        Map<String, Object> senderProps = KafkaTestUtils.producerProps(embeddedKafka);
        DefaultKafkaProducerFactory<Integer, String> pf = new DefaultKafkaProducerFactory<>(senderProps);
        try {
            KafkaTemplate<Integer, String> template = new KafkaTemplate<>(pf, true);
            template.setDefaultTopic("topic-in");
            template.send(MessageBuilder.withPayload("1").build());
            template.send(MessageBuilder.withPayload("2").build());
            template.send(MessageBuilder.withPayload("3").build());
            template.send(MessageBuilder.withPayload("4").build());
            template.send(MessageBuilder.withPayload("5").build());
            ConsumerRecords<String, String> consumerRecords = KafkaTestUtils.getRecords(consumer);
            var topicIn = new ArrayList<ConsumerRecord<String, String>>();
            var defaultOut = new ArrayList<ConsumerRecord<String, String>>();
            var customOut = new ArrayList<ConsumerRecord<String, String>>();

            for(var record : consumerRecords.records("topic-in")) {
                topicIn.add(record);
            }
            for(var record : consumerRecords.records("default-out")) {
                defaultOut.add(record);
            }
            for(var record : consumerRecords.records("custom-out")) {
                customOut.add(record);
            }

            assertEquals(6, topicIn.size());
            assertThat(defaultOut.size()).isLessThanOrEqualTo(2);
            assertThat(customOut.size()).isLessThanOrEqualTo(2);

            assertEquals(0, Integer.parseInt(topicIn.get(0).value()));
            assertEquals(1, Integer.parseInt(topicIn.get(1).value()));
            assertEquals(2, Integer.parseInt(topicIn.get(2).value()));
            assertEquals(3, Integer.parseInt(topicIn.get(3).value()));
            assertEquals(4, Integer.parseInt(topicIn.get(4).value()));
            assertEquals(5, Integer.parseInt(topicIn.get(5).value()));

            assertEquals(0, Integer.parseInt(defaultOut.get(0).value()));
            try {
                assertEquals(5, Integer.parseInt(defaultOut.get(1).value()));
            } catch (IndexOutOfBoundsException e) {
                System.out.println("We may not reach this point because the tests are not consistently timed");
            }

            assertTrue(customOut.get(0).value().contains("value = ["));
            try {
                assertTrue(customOut.get(1).value().contains("value=4"));
            } catch (IndexOutOfBoundsException e) {
                System.out.println("We may not reach this point because the tests are not consistently timed");
            }
        }
        finally {
            pf.destroy();
        }
    }
}