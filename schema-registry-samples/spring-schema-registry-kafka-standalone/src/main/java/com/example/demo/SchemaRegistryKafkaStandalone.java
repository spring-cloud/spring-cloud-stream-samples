package com.example.demo;

import java.util.Random;
import java.util.function.Consumer;

import com.example.Sensor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.schema.registry.client.EnableSchemaRegistryClient;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.util.MimeType;

@SpringBootApplication
@EnableSchemaRegistryClient
public class SchemaRegistryKafkaStandalone implements CommandLineRunner {

	@Autowired
	private StreamBridge streamBridge;

	public static void main(String[] args) {
		SpringApplication.run(SchemaRegistryKafkaStandalone.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Random random = new Random();
		Sensor sensor = Sensor.newBuilder()
				.setId("sensor")
				.setAcceleration(random.nextFloat())
				.setTemperature(random.nextFloat())
				.setVelocity(random.nextFloat())
				.build();
		send(sensor);
	}

	void send(Sensor sensor) {
		streamBridge.send("sensor", sensor, MimeType.valueOf("application/+avro"));
	}

	@Bean
	public Consumer<Sensor> consume() {
		return (sensor) -> {
			System.out.println("Received Type:" + sensor.getClass().getCanonicalName());
			System.out.println("Received:" + sensor);
		};
	}

}
