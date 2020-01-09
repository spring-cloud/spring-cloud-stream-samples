package com.example.kafkanativeserialization;

import java.util.function.Function;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KafkaNativeSerializationApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaNativeSerializationApplication.class, args);
	}

	@Bean
	public Function<String, Person> process() {
		return str -> {
			Person item = new Person(str);
			return item;
		};
	}

}
