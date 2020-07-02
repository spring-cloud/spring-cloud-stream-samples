package com.example.stream.bridge.avro;

import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.example.Sensor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StreamBridgeAvroApplication {

	private Random random = new Random();

	@Autowired
	StreamBridge streamBridge;

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

	@Bean
	public Consumer<Sensor> receiveAndForward() {
		return s -> streamBridge.send("sensor-out-0", s);
	}

	@Bean
	Consumer<Sensor> receive() {
		return s -> System.out.println("Received Sensor: " + s);
	}

	public static void main(String[] args) {
		SpringApplication.run(StreamBridgeAvroApplication.class, args);
	}

}
