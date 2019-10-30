package demo.producer;

import java.util.Random;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author Soby Chacko
 */
@SpringBootApplication
public class PartitioningProducerApplication {

	private static final Random RANDOM = new Random(System.currentTimeMillis());

	private static final Logger logger = LoggerFactory.getLogger(PartitioningProducerApplication.class);


	public static void main(String[] args) {
		SpringApplication.run(PartitioningProducerApplication.class, args);
	}

	// We use a strategy so that this data will end up in a partition,
	// P = L(x) - 1 where L is a length function on the payload.
	private static final String[] data = new String[]{
			"f", "g", "h", //making them go to partition-0 by making a single char string
			"fo", "go", "ho",
			"foo", "goo", "hoo",
			"fooz", "gooz", "hooz"
	};

	@Bean
	public Supplier<Message<?>> generate() {
		return () -> {
			String value = data[RANDOM.nextInt(data.length)];
			logger.info("Sending: " + value);
			return MessageBuilder.withPayload(value)
					.setHeader("partitionKey", value.length())
					.build();
		};
	}
}
