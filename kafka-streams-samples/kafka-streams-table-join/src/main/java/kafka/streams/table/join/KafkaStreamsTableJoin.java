/*
 * Copyright 2018 the original author or authors.
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

package kafka.streams.table.join;

import java.util.function.BiFunction;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KafkaStreamsTableJoin {

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsTableJoin.class, args);
	}

	public static class KStreamToTableJoinApplication {

		@Bean
		public BiFunction<KStream<String, Long>, KTable<String, String>, KStream<String, Long>> process() {

			return (userClicksStream, userRegionsTable) -> userClicksStream
					.leftJoin(userRegionsTable,
							(clicks, region) -> new RegionWithClicks(region == null ? "UNKNOWN" : region, clicks),
							Joined.with(Serdes.String(), Serdes.Long(), null))
					.map((user, regionWithClicks) -> new KeyValue<>(regionWithClicks.getRegion(), regionWithClicks.getClicks()))
					.groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
					.reduce((firstClicks, secondClicks) -> firstClicks + secondClicks)
					.toStream();
		}
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
