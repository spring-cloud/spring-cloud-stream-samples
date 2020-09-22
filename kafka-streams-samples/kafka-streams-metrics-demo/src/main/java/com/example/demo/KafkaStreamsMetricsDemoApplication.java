package com.example.demo;

import java.util.function.Function;

import org.apache.kafka.streams.kstream.KStream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KafkaStreamsMetricsDemoApplication {

	static int count = 0;

	public static void main(String[] args) {
		SpringApplication.run(KafkaStreamsMetricsDemoApplication.class, args);
	}

	@Bean
	public Function<KStream<String, String>, KStream<String, String>> process() {
		return input ->
				input.peek((key,value)->
						System.out.println("Key: " + key + "\t\tValue: " + value + "\t\tMessage Count: " + count++));
	}

}
