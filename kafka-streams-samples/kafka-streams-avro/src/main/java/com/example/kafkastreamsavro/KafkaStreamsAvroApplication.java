package com.example.kafkastreamsavro;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.example.Sensor;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.kstream.KStream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KafkaStreamsAvroApplication {

	private Random random = new Random();

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsAvroApplication.class, args);
	}

	@Bean
	public Consumer<KStream<Sensor, Sensor>> process(){
		return input -> {
			input.peek(((key, value) -> System.out.println(" value: "+value.toString())));
		};
	}

	@Bean
	public Serde<Sensor> avroInSerde(){
		final SpecificAvroSerde<Sensor> avroInSerde = new SpecificAvroSerde<>();
		Map<String, Object> serdeProperties = new HashMap<>();
		return avroInSerde;
	}

	@Bean
	public Supplier<Sensor> supplier() {
		return () -> {
			Sensor sensor = new Sensor();
			sensor.setId(UUID.randomUUID().toString() + "-v1");
			sensor.setAcceleration(random.nextFloat() * 10);
			sensor.setVelocity(random.nextFloat() * 100);
			sensor.setTemperature(random.nextFloat() * 50);
			return sensor;
		};
	}
}
