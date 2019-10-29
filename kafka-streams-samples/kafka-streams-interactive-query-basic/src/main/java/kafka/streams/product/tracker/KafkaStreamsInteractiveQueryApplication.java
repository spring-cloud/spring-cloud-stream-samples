/*
 * Copyright 2017 the original author or authors.
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

package kafka.streams.product.tracker;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

@SpringBootApplication
public class KafkaStreamsInteractiveQueryApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsInteractiveQueryApplication.class, args);
	}

	@EnableConfigurationProperties(ProductTrackerProperties.class)
	@EnableScheduling
	public static class InteractiveProductCountApplication {

		private static final String STORE_NAME = "prod-id-count-store";

		@Autowired
		private InteractiveQueryService queryService;

		@Autowired
		ProductTrackerProperties productTrackerProperties;

		ReadOnlyKeyValueStore<Object, Object> keyValueStore;

		@Bean
		public Function<KStream<Object, Product>, KStream<Integer, Long>> process() {

			return input -> input
					.filter((key, product) -> productIds().contains(product.getId()))
					.map((key, value) -> new KeyValue<>(value.id, value))
					.groupByKey(Grouped.with(Serdes.Integer(), new JsonSerde<>(Product.class)))
					.count(Materialized.<Integer, Long, KeyValueStore<Bytes, byte[]>>as(STORE_NAME)
						.withKeySerde(Serdes.Integer())
						.withValueSerde(Serdes.Long()))
					.toStream();
		}

		private Set<Integer> productIds() {
			return StringUtils.commaDelimitedListToSet(productTrackerProperties.getProductIds())
					.stream().map(Integer::parseInt).collect(Collectors.toSet());
		}


		@Scheduled(fixedRate = 30000, initialDelay = 5000)
		public void printProductCounts() {
			if (keyValueStore == null) {
				keyValueStore = queryService.getQueryableStore(STORE_NAME, QueryableStoreTypes.keyValueStore());
			}

			for (Integer id : productIds()) {
				System.out.println("Product ID: " + id + " Count: " + keyValueStore.get(id));
			}
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
}
