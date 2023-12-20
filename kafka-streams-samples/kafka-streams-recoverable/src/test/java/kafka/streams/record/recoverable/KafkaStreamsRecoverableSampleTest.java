package kafka.streams.record.recoverable;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        controlledShutdown = true,
        topics = {"topic-in", "default-out", "custom-out", "error-default-out", "error-custom-out"}
)
@TestPropertySource(properties = {"config.enableSupplier=false"})
public class KafkaStreamsRecoverableSampleTest {

    @Autowired
    EmbeddedKafkaBroker embeddedKafka;

    private static Consumer<String, String> consumer;

    @AfterEach
    public static void tearDown() {
        consumer.close();
        System.clearProperty("spring.cloud.stream.kafka.streams.binder.brokers");
    }

    @Test
    public void testKafkaStreams() throws InterruptedException {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("group", "false", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        DefaultKafkaConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = cf.createConsumer();
        embeddedKafka.consumeFromEmbeddedTopics(consumer, "topic-in", "default-out", "custom-out",
                "error-default-out", "error-custom-out");
        System.setProperty("spring.cloud.stream.kafka.streams.binder.brokers", embeddedKafka.getBrokersAsString());

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

            sleep(15000);

            ConsumerRecords<String, String> consumerRecords = KafkaTestUtils.getRecords(consumer);
            var topicIn = new ArrayList<ConsumerRecord<String, String>>();
            var defaultOut = new ArrayList<ConsumerRecord<String, String>>();
            var customOut = new ArrayList<ConsumerRecord<String, String>>();
            var errorDefaultOut = new ArrayList<ConsumerRecord<String, String>>();
            var errorCustomOut = new ArrayList<ConsumerRecord<String, String>>();

            for(var record : consumerRecords.records("topic-in")) {
                topicIn.add(record);
            }
            for(var record : consumerRecords.records("default-out")) {
                defaultOut.add(record);
            }
            for(var record : consumerRecords.records("custom-out")) {
                customOut.add(record);
            }
            for(var record : consumerRecords.records("error-default-out")) {
                errorDefaultOut.add(record);
            }
            for(var record : consumerRecords.records("error-custom-out")) {
                errorCustomOut.add(record);
            }

            assertThat(topicIn.size()).isEqualTo(5);
            assertThat(defaultOut.size()).isEqualTo(4);
            assertThat(customOut.size()).isEqualTo(4);
            assertThat(errorDefaultOut.size()).isEqualTo(1);
            assertThat(errorCustomOut.size()).isEqualTo(1);

            validateTopicIn(topicIn);
            validateDefaultOut(defaultOut);
            validateCustomOut(customOut);
            validateErrorDefaultOut(errorDefaultOut);
            validateErrorCustomOut(errorCustomOut);
        }
        finally {
            pf.destroy();
        }
    }

    void validateTopicIn(List<ConsumerRecord<String, String>> records) {
        assertThat(Integer.parseInt(records.get(0).value())).isEqualTo(1);
        assertThat(Integer.parseInt(records.get(1).value())).isEqualTo(2);
        assertThat(Integer.parseInt(records.get(2).value())).isEqualTo(3);
        assertThat(Integer.parseInt(records.get(3).value())).isEqualTo(4);
        assertThat(Integer.parseInt(records.get(4).value())).isEqualTo(5);
    }

    void validateDefaultOut(List<ConsumerRecord<String, String>> records) {
        assertThat(records.get(0).value()).isEqualTo("Success");
        assertThat(records.get(1).value()).isEqualTo("Success");
        assertThat(records.get(2).value()).isEqualTo("Success");
        assertThat(records.get(3).value()).isEqualTo("Success");
    }

    void validateCustomOut(List<ConsumerRecord<String, String>> records) {
        assertThat(records.get(0).value()).isEqualTo("Success");
        assertThat(records.get(1).value()).isEqualTo("Success");
        assertThat(records.get(2).value()).isEqualTo("Success");
        assertThat(records.get(3).value()).isEqualTo("Success");
    }

    void validateErrorDefaultOut(List<ConsumerRecord<String, String>> records) {
        assertThat(Integer.parseInt(records.get(0).value())).isEqualTo(5);
    }

    void validateErrorCustomOut(List<ConsumerRecord<String, String>> records) {
        assertTrue(records.get(0).value().contains("value=4"));
        assertTrue(records.get(0).value().contains("Something went wrong, Error"));
    }
}