package kafka.streams.table.join;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Serialized;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.messaging.handler.annotation.SendTo;

@SpringBootApplication
public class KafkaStreamsTableJoin {

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsTableJoin.class, args);
	}

	@EnableBinding(KStreamProcessorX.class)
	@EnableAutoConfiguration
	public static class KStreamToTableJoinApplication {


		@StreamListener
		@SendTo("output")
		public KStream<String, Long> process(@Input("input") KStream<String, Long> userClicksStream,
											 @Input("inputX") KTable<String, String> userRegionsTable) {

			return userClicksStream
					.leftJoin(userRegionsTable,
							(clicks, region) -> new RegionWithClicks(region == null ? "UNKNOWN" : region, clicks),
							Joined.with(Serdes.String(), Serdes.Long(), null))
					.map((user, regionWithClicks) -> new KeyValue<>(regionWithClicks.getRegion(), regionWithClicks.getClicks()))
					.groupByKey(Serialized.with(Serdes.String(), Serdes.Long()))
					.reduce((firstClicks, secondClicks) -> firstClicks + secondClicks)
					.toStream();
		}
	}


	interface KStreamProcessorX extends KafkaStreamsProcessor {

		@Input("inputX")
		KTable<?, ?> inputX();
	}

	private static final class RegionWithClicks {

		private final String region;
		private final long clicks;

		public RegionWithClicks(String region, long clicks) {
			if (region == null || region.isEmpty()) {
				throw new IllegalArgumentException("region must be set");
			}
			if (clicks < 0) {
				throw new IllegalArgumentException("clicks must not be negative");
			}
			this.region = region;
			this.clicks = clicks;
		}

		public String getRegion() {
			return region;
		}

		public long getClicks() {
			return clicks;
		}

	}
}
