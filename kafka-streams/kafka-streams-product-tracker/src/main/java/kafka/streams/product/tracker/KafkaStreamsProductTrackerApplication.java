/*
 * Copyright 2017 the original author or authors.
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

package kafka.streams.product.tracker;

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
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootApplication
public class KafkaStreamsProductTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsProductTrackerApplication.class, args);
	}

	@EnableBinding(KafkaStreamsProcessor.class)
	@EnableAutoConfiguration
	@EnableConfigurationProperties(ProductTrackerProperties.class)
	public static class ProductCountApplication {

		@Autowired
		ProductTrackerProperties productTrackerProperties;

		@Autowired
		TimeWindows timeWindows;

		@StreamListener("input")
		@SendTo("output")
		public KStream<Integer, ProductStatus> process(KStream<Object, Product> input) {
			return input
					.filter((key, product) -> productIds().contains(product.getId()))
					.map((key, value) -> new KeyValue<>(value, value))
					.groupByKey(new JsonSerde<>(Product.class), new JsonSerde<>(Product.class))
					.count(timeWindows, "product-counts")
					.toStream()
					.map((key, value) -> new KeyValue<>(key.key().id, new ProductStatus(key.key().id,
							value, Instant.ofEpochMilli(key.window().start()).atZone(ZoneId.systemDefault()).toLocalTime(),
							Instant.ofEpochMilli(key.window().end()).atZone(ZoneId.systemDefault()).toLocalTime())));
		}

		private Set<Integer> productIds() {
			return StringUtils.commaDelimitedListToSet(productTrackerProperties.getProductIds())
				.stream().map(Integer::parseInt).collect(Collectors.toSet());
		}

	}

	@ConfigurationProperties(prefix = "app.product.tracker")
	static class  ProductTrackerProperties {

		private String productIds;

		public String getProductIds() {
			return productIds;
		}

		public void setProductIds(String productIds) {
			this.productIds = productIds;
		}

	}

	static class Product {

		Integer id;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}
	}

	static class ProductStatus {
		private Integer id;
		private long count;
		private LocalTime windowStart;
		private LocalTime windowEnd;

		public ProductStatus(Integer id, long count, LocalTime windowStart, LocalTime windowEnd) {
			this.id = id;
			this.count = count;
			this.windowStart = windowStart;
			this.windowEnd = windowEnd;
		}

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public long getCount() {
			return count;
		}

		public void setCount(long count) {
			this.count = count;
		}

		public LocalTime getWindowStart() {
			return windowStart;
		}

		public void setWindowStart(LocalTime windowStart) {
			this.windowStart = windowStart;
		}

		public LocalTime getWindowEnd() {
			return windowEnd;
		}

		public void setWindowEnd(LocalTime windowEnd) {
			this.windowEnd = windowEnd;
		}
	}

}
