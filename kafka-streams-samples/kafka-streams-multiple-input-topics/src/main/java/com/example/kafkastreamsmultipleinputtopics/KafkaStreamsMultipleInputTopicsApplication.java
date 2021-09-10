package com.example.kafkastreamsmultipleinputtopics;

import java.util.function.Consumer;

import org.apache.kafka.streams.kstream.KStream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KafkaStreamsMultipleInputTopicsApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsMultipleInputTopicsApplication.class, args);
	}

	@Bean
	public Consumer<KStream<String, String>> routeRequests() {
		return uda -> uda.foreach((s, request) -> {
			System.out.println("Hello:" + request);
		});
	}
}
