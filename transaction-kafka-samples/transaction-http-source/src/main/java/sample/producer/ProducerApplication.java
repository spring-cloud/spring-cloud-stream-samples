package sample.producer;

import java.util.Random;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableBinding(Source.class)
@RestController
public class ProducerApplication {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private Source source;

    public ProducerApplication(Source source) {

        this.source = source;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }

    @PostMapping()
    public String sendMessage(@RequestBody PersonEvent incomming) {
        PersonEvent personEvent = new PersonEvent();
        personEvent.setType("CreatePerson");
        personEvent.setName(incomming.getName());
        source.output().send(MessageBuilder.withPayload(personEvent).build());
        logger.info("Person sendt={}", personEvent);
        return "Person sendt";
    }

    static class PersonEvent {

        String name;
        String type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", PersonEvent.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("type='" + type + "'")
                .toString();
        }
    }
}

