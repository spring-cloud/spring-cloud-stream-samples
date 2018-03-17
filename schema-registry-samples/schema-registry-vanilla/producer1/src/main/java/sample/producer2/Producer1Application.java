package sample.producer2;

import com.example.Sensor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.schema.client.EnableSchemaRegistryClient;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.UUID;

@SpringBootApplication
@EnableSchemaRegistryClient
@EnableBinding(Source.class)
@RestController
public class Producer1Application {

	@Autowired
	private Source source;

	private Random random = new Random();

	public static void main(String[] args) {
		SpringApplication.run(Producer1Application.class, args);
	}

	@RequestMapping(value = "/messages", method = RequestMethod.POST)
	public String sendMessage() {
		source.output().send(MessageBuilder.withPayload(randomSensor()).build());
		return "ok, have fun with v1 payload!";
	}

	private Sensor randomSensor() {
		Sensor sensor = new Sensor();
		sensor.setId(UUID.randomUUID().toString() + "-v1");
		sensor.setAcceleration(random.nextFloat() * 10);
		sensor.setVelocity(random.nextFloat() * 100);
		sensor.setTemperature(random.nextFloat() * 50);
		return sensor;
	}

	//Another convenience POST method for testing deterministic values
	@RequestMapping(value = "/messagesX", method = RequestMethod.POST)
	public String sendMessageX(@RequestParam(value="id") String id, @RequestParam(value="acceleration") float acceleartion,
							   @RequestParam(value="velocity") float velocity, @RequestParam(value="temperature") float temperature) {
		Sensor sensor = new Sensor();
		sensor.setId(id + "-v1");
		sensor.setAcceleration(acceleartion);
		sensor.setVelocity(velocity);
		sensor.setTemperature(temperature);
		source.output().send(MessageBuilder.withPayload(sensor).build());
		return "ok, have fun with v1 payload!";
	}
}

