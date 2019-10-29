package reactive.kafka;

import java.time.Duration;
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

@SpringBootApplication
public class ReactiveProcessorApplication {

	private final Log logger = LogFactory.getLog(getClass());

	private AtomicBoolean semaphore = new AtomicBoolean(true);

	public static void main(String[] args) {
		SpringApplication.run(ReactiveProcessorApplication.class, args);
	}

	@Bean
	public Function<Flux<String>, Flux<String>> aggregate() {
		return inbound -> inbound.
				log()
				.window(Duration.ofSeconds(30), Duration.ofSeconds(5))
				.flatMap(w -> w.reduce("", (s1,s2)->s1+s2))
				.log();
	}

	//Following source and sinks are used for testing only.
	//Test source will send data to the same destination where the processor receives data
	//Test sink will consume data from the same destination where the processor produces data

	@Bean
	public Supplier<String> testSource() {
		return () -> this.semaphore.getAndSet(!this.semaphore.get()) ? "foo" : "bar";

	}

	@Bean
	public Consumer<String>  testSink() {
		return payload -> logger.info("Data received: " + payload);

	}
}
