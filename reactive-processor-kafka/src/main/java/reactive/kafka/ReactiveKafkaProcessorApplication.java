package reactive.kafka;

import java.time.Duration;

import reactor.core.publisher.Flux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;

@SpringBootApplication
@EnableBinding(Processor.class)
public class ReactiveKafkaProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveKafkaProcessorApplication.class, args);
	}

	@StreamListener
	@Output(Processor.OUTPUT)
	public Flux<String> toUpperCase(@Input(Processor.INPUT) Flux<String> inbound) {
		return inbound.
				log()
				.window(Duration.ofSeconds(10), Duration.ofSeconds(5))
				.flatMap(w -> w.reduce("", (s1,s2)->s1+s2))
				.log();
	}
}
