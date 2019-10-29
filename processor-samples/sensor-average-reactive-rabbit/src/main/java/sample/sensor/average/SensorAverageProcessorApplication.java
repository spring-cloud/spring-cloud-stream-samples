package sample.sensor.average;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SensorAverageProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SensorAverageProcessorApplication.class, args);
	}

	@Bean
	public Function<Flux<Sensor>, Flux<Average>> calculateAverage() {
		return data -> data.window(Duration.ofSeconds(3)).flatMap(
				window -> window.groupBy(Sensor::getId).flatMap(this::calculateAverage));
	}

	private Mono<Average> calculateAverage(GroupedFlux<Integer, Sensor> group) {
		return group
				.reduce(new Accumulator(0, 0),
						(a, d) -> new Accumulator(a.getCount() + 1, a.getTotalValue() + d.getTemperature()))
				.map(accumulator -> new Average(group.key(), (accumulator.getTotalValue()) / accumulator.getCount()));
	}

	static class Accumulator {

		private int count;

		private int totalValue;

		public Accumulator(int count, int totalValue) {
			this.count = count;
			this.totalValue = totalValue;
		}

		/**
		 * @return the count
		 */
		public int getCount() {
			return count;
		}

		/**
		 * @param count the count to set
		 */
		public void setCount(int count) {
			this.count = count;
		}

		/**
		 * @return the totalValue
		 */
		public int getTotalValue() {
			return totalValue;
		}

		/**
		 * @param totalValue the totalValue to set
		 */
		public void setTotalValue(int totalValue) {
			this.totalValue = totalValue;
		}
	}

	static class Average {

		private int id;

		private double average;

		public Average(int id, double average) {
			this.id = id;
			this.average = average;
		}

		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(int id) {
			this.id = id;
		}

		/**
		 * @return the average
		 */
		public double getAverage() {
			return average;
		}

		/**
		 * @param average the average to set
		 */
		public void setAverage(double average) {
			this.average = average;
		}
	}

	static class Sensor {

		private int id;

		private int temperature;

		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(int id) {
			this.id = id;
		}

		/**
		 * @return the temperature
		 */
		public int getTemperature() {
			return temperature;
		}

		/**
		 * @param temperature the temperature to set
		 */
		public void setTemperature(int temperature) {
			this.temperature = temperature;
		}
	}

	//Following source and sinks are used for testing only.
	//Test source will send data to the same destination where the processor receives data
	//Test sink will consume data from the same destination where the processor produces data

	static class TestSource {

		private AtomicBoolean semaphore = new AtomicBoolean(true);
		private Random random = new Random();
		private int[] ids = new int[]{100100, 100200, 100300};

		@Bean
		public Supplier<Sensor> sendTestData() {

			return () -> {
				int id = ids[random.nextInt(3)];
				int temperature = random.nextInt((102 - 65) + 1) + 65;
				Sensor sensor = new Sensor();
				sensor.setId(id);
				sensor.setTemperature(temperature);
				return sensor;
			};
		}
	}

	static class TestSink {

		private final Log logger = LogFactory.getLog(getClass());

		@Bean
		public Consumer<String> receive() {
			return payload -> logger.info("Data received: " + payload);
		}
	}
}
