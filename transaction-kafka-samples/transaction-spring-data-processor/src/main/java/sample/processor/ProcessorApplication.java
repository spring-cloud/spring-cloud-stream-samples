package sample.processor;

import java.util.StringJoiner;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableBinding(Processor.class)
@EnableTransactionManagement
public class ProcessorApplication {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private PersonRepository repository;

    public ProcessorApplication(PersonRepository repository) {

        this.repository = repository;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProcessorApplication.class, args);
    }

    @Transactional
    @StreamListener(Processor.INPUT)
    @SendTo(Processor.OUTPUT)
    public PersonEvent process(PersonEvent data) {
        logger.info("Received event={}", data);
        Person person = new Person();
        person.setName(data.getName());
        logger.info("Saving person={}", person);
        Person savedPerson = repository.save(person);

        PersonEvent event = new PersonEvent();
        event.setName(savedPerson.getName());
        event.setType("PersonSaved");
        logger.info("Sent event={}", event);
        return event;
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
