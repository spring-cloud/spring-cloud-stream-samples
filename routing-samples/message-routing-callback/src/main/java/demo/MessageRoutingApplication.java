package demo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@Slf4j
@SpringBootApplication
public class MessageRoutingApplication {

	public static void main(String[] args) {
		final ConfigurableApplicationContext run = SpringApplication.run(MessageRoutingApplication.class, args);
		final KafkaTemplate kafkaTemplate = run.getBean(KafkaTemplate.class);
		kafkaTemplate.setDefaultTopic("functionRouter-in-0");

		Message<Menu> menuMessage =
				MessageBuilder.withPayload(new Menu())
						.setHeader("event_type", "MENU_EVENT").build();

		kafkaTemplate.send(menuMessage);

		Message<Order> orderMessage =
				MessageBuilder.withPayload(new Order())
						.setHeader("event_type", "ORDER_EVENT").build();

		kafkaTemplate.send(orderMessage);

		System.out.println();
	}


	@Bean
	public Map<String, Object> producerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		return props;
	}

	@Bean
	public ProducerFactory<String, Object> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	@Bean
	public MessageRoutingCallback messageRoutingCallback() {
		return new CustomMessageRoutingCallback();
	}

	@Bean
	public Consumer<Order> orderConsumer(){
		return order -> log.info(order.toString());
	}

	@Bean
	public Consumer<Menu> menuConsumer(){
		return menu -> log.info(menu.toString());
	}

	@Bean
	public Supplier<Message<Menu>> supply() {
		return () -> MessageBuilder.withPayload(new Menu()).setHeader("event_type", "MENU_EVENT").build();
	}
}

@Data
class Order {
	private String id;
	private Double price;
}

@Data
class Menu {
	private String id;
	private String name;
}
