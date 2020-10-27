/*
 * Copyright 2019 the original author or authors.
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

package kafka.streams.word.count;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;



/**
 * TopologyTestDriver based test about stream processing of {@link KafkaStreamsWordCountApplication}
 *
 * @author Jukka Karvanen / jukinimi.com
 */

public class WordCountProcessorApplicationTests {
    private TopologyTestDriver testDriver;
    public static final String INPUT_TOPIC = KafkaStreamsWordCountApplication.WordCountProcessorApplication.INPUT_TOPIC;
    public static final String OUTPUT_TOPIC = KafkaStreamsWordCountApplication.WordCountProcessorApplication.OUTPUT_TOPIC;
    private TestInputTopic inputTopic;
    private TestOutputTopic outputTopic;

    final Serde<String> stringSerde = Serdes.String();
    final JsonSerde<KafkaStreamsWordCountApplication.WordCount> countSerde = new JsonSerde<>(KafkaStreamsWordCountApplication.WordCount.class);
    final Serde<Bytes> nullSerde = Serdes.Bytes(); //Serde for not used key

    static Properties getStreamsConfiguration() {
        final Properties streamsConfiguration = new Properties();
        // Need to be set even these do not matter with TopologyTestDriver
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "TopologyTestDriver");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        return streamsConfiguration;
    }

    /**
     * Setup Stream topology
     * Add KStream based on @StreamListener annotation
     * Add to(topic) based @SendTo annotation
     */
    @Before
    public void setup() {
        final StreamsBuilder builder = new StreamsBuilder();
        buildStreamProcessingPipeline(builder);

        testDriver = new TopologyTestDriver(builder.build(), getStreamsConfiguration());
        inputTopic = testDriver.createInputTopic(INPUT_TOPIC, nullSerde.serializer(), stringSerde.serializer());
        outputTopic = testDriver.createOutputTopic(OUTPUT_TOPIC, nullSerde.deserializer(), countSerde.deserializer());
    }

    private void buildStreamProcessingPipeline(StreamsBuilder builder) {
        KStream<Bytes, String> input = builder.stream(INPUT_TOPIC, Consumed.with(nullSerde, stringSerde));
        KafkaStreamsWordCountApplication.WordCountProcessorApplication app = new KafkaStreamsWordCountApplication.WordCountProcessorApplication();
        final Function<KStream<Bytes, String>, KStream<Bytes, KafkaStreamsWordCountApplication.WordCount>> process = app.process();
        final KStream<Bytes, KafkaStreamsWordCountApplication.WordCount> output = process.apply(input);
        output.to(OUTPUT_TOPIC, Produced.with(nullSerde, countSerde));
    }

    @After
    public void tearDown() {
        try {
            testDriver.close();
        } catch (final RuntimeException e) {
            // https://issues.apache.org/jira/browse/KAFKA-6647 causes exception when executed in Windows, ignoring it
            // Logged stacktrace cannot be avoided
            System.out.println("Ignoring exception, test failing in Windows due this exception:" + e.getLocalizedMessage());
        }
    }

    /**
     * Simple test validating count of one word
     */
    @Test
    public void testOneWord() {
        final String nullKey = null;
        //Feed word "Hello" to inputTopic and no kafka key, timestamp is irrelevant in this case
        inputTopic.pipeInput(nullKey, "Hello", 1L);

        //Read and validate output
        final Object output = outputTopic.readValue();
        assertThat(output).isNotNull();
        assertThat(output).isEqualToComparingFieldByField(new KafkaStreamsWordCountApplication.WordCount("hello", 1L, new Date(0), new Date(KafkaStreamsWordCountApplication.WordCountProcessorApplication.WINDOW_SIZE_MS)));
        //No more output in topic
        assertThat(outputTopic.isEmpty()).isTrue();
    }

    /**
     * Test Word count of sentence list.
     */
    @Test
    public void shouldCountWords() {
        final List<String> inputLines = Arrays.asList(
                "Kafka Streams Examples",
                "Spring Cloud Stream Sample",
                "Using Kafka Streams Test Utils"
        );
        final List<KeyValue<String, String>> inputRecords = inputLines.stream().map(v -> new KeyValue<String, String>(null, v)).collect(Collectors.toList());

        final Map<String, Long> expectedWordCounts = new HashMap<>();
        expectedWordCounts.put("spring", 1L);
        expectedWordCounts.put("cloud", 1L);
        expectedWordCounts.put("examples", 1L);
        expectedWordCounts.put("sample", 1L);
        expectedWordCounts.put("streams", 2L);
        expectedWordCounts.put("stream", 1L);
        expectedWordCounts.put("test", 1L);
        expectedWordCounts.put("utils", 1L);
        expectedWordCounts.put("kafka", 2L);
        expectedWordCounts.put("using", 1L);

        inputTopic.pipeKeyValueList(inputRecords, Instant.ofEpochSecond(1L), Duration.ofMillis(1000L));
        final Map<String, Long> actualWordCounts = getOutputList();
        assertThat(actualWordCounts).containsAllEntriesOf(expectedWordCounts).hasSameSizeAs(expectedWordCounts);
    }

    /**
     * Read counts from output to map ignoring start and end dates
     * If existing word is incremented, it can appear twice in output and is replaced in map
     *
     * @return Map of Word and counts
     */
    private Map<String, Long> getOutputList() {
        final Map<String, Long> output = new HashMap<>();
        KafkaStreamsWordCountApplication.WordCount outputRow;
        while (!outputTopic.isEmpty()) {
            outputRow = (KafkaStreamsWordCountApplication.WordCount) outputTopic.readValue();
            output.put(outputRow.getWord(), outputRow.getCount());
        }
        return output;
    }
}
