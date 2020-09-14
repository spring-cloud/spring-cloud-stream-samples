package kafka.ssl.demo;

import java.util.function.Consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KafkaSslDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaSslDemoApplication.class, args);
	}

	@Bean
	public Consumer<String> consumer() {
		return s -> System.out.println("Message Received: " + s);
	}
}
