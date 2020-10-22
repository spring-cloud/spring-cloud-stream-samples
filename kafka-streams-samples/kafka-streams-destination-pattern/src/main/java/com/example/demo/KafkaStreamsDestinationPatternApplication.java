package com.example.demo;

import java.util.function.Consumer;

import org.apache.kafka.streams.kstream.KStream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KafkaStreamsDestinationPatternApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsDestinationPatternApplication.class, args);
	}

	@Bean
	public Consumer<KStream<String, String>> stream() {
		return in -> in.foreach((k, v) -> System.out.println("Got this record: " + v));
	}

}