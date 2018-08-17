package demo.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Random;

/**
 * @author Soby Chacko
 */
public class Producer {

	private static final Logger logger = LoggerFactory.getLogger(Producer.class);

	@EnableBinding(Source.class)
	static class KafkaPartitionProducerApplication {

		private static final Random RANDOM = new Random(System.currentTimeMillis());

		// We use a strategy so that this data will end up in a partition,
		// P = L(x) - 1 where L is a length function on the payload.
		private static final String[] data = new String[]{
				"f", "g", "h", //making them go to partition-0 by making a single char string
				"fo", "go", "ho",
				"foo", "goo", "hoo",
				"fooz", "gooz", "hooz"
		};

		@InboundChannelAdapter(channel = Source.OUTPUT, poller = @Poller(fixedRate = "1000"))
		public Message<?> generate() {
			String value = data[RANDOM.nextInt(data.length)];
			logger.info("Sending: " + value);
			return MessageBuilder.withPayload(value)
					.setHeader("partitionKey", value.length())
					.build();
		}
	}
}
