package kstream.word.count.kstreamwordcount;

import java.util.Arrays;
import java.util.Date;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.TimeWindows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kstream.annotations.KStreamProcessor;
import org.springframework.messaging.handler.annotation.SendTo;

@SpringBootApplication
public class KStreamWordCountApplication {

	public static void main(String[] args) {
		SpringApplication.run(KStreamWordCountApplication.class, args);
	}

	@EnableBinding(KStreamProcessor.class)
	@EnableAutoConfiguration
	@EnableConfigurationProperties(WordCountProcessorProperties.class)
	public static class WordCountProcessorApplication {

		@Autowired
		private WordCountProcessorProperties processorProperties;

		@StreamListener("input")
		@SendTo("output")
		public KStream<?, WordCount> process(KStream<Object, String> input) {

			return input
					.flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
					.map((key, value) -> new KeyValue<>(value, value))
					.groupByKey(Serdes.String(), Serdes.String())
					.count(configuredTimeWindow(), processorProperties.getStoreName())
					.toStream()
					.map((key, value) -> new KeyValue<>(null, new WordCount(key.key(), value, new Date(key.window().start()), new Date(key.window().end()))));
		}

		/**
		 * Constructs a {@link TimeWindows} property.
		 *
		 * @return
		 */
		private TimeWindows configuredTimeWindow() {
			return processorProperties.getAdvanceBy() > 0
					? TimeWindows.of(processorProperties.getWindowLength()).advanceBy(processorProperties.getAdvanceBy())
					: TimeWindows.of(processorProperties.getWindowLength());
		}
	}

	@ConfigurationProperties(prefix = "kstream.word.count")
	static class  WordCountProcessorProperties {

		private int windowLength = 5000;

		private int advanceBy = 0;

		private String storeName = "WordCounts";

		int getWindowLength() {
			return windowLength;
		}

		public void setWindowLength(int windowLength) {
			this.windowLength = windowLength;
		}

		int getAdvanceBy() {
			return advanceBy;
		}

		public void setAdvanceBy(int advanceBy) {
			this.advanceBy = advanceBy;
		}

		String getStoreName() {
			return storeName;
		}

		public void setStoreName(String storeName) {
			this.storeName = storeName;
		}
	}

	static class WordCount {

		private String word;

		private long count;

		private Date start;

		private Date end;

		WordCount(String word, long count, Date start, Date end) {
			this.word = word;
			this.count = count;
			this.start = start;
			this.end = end;
		}

		public String getWord() {
			return word;
		}

		public void setWord(String word) {
			this.word = word;
		}

		public long getCount() {
			return count;
		}

		public void setCount(long count) {
			this.count = count;
		}

		public Date getStart() {
			return start;
		}

		public void setStart(Date start) {
			this.start = start;
		}

		public Date getEnd() {
			return end;
		}

		public void setEnd(Date end) {
			this.end = end;
		}
	}
}
