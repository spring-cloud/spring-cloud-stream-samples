/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sample.processor;

import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.config.ListenerContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.DefaultAfterRollbackProcessor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.backoff.FixedBackOff;

/**
 * @author Bjarte Stien Karlsen
 */
@SpringBootApplication
@EnableTransactionManagement
public class ProcessorApplication {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private AtomicBoolean shouldFail= new AtomicBoolean(false);
    private PersonRepository repository;

    public ProcessorApplication(PersonRepository repository) {

        this.repository = repository;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProcessorApplication.class, args);
    }

    @Transactional
    @Bean
    public Function<PersonEvent, PersonEvent> process() {
        return pe -> {
            logger.info("Received event={}", pe);
            Person person = new Person();
            person.setName(pe.getName());

            if (shouldFail.get()) {
                shouldFail.set(false);
                throw new RuntimeException("Simulated network error");
            } else {
                //We fail every other request as a test
                shouldFail.set(true);
            }
            logger.info("Saving person={}", person);

            Person savedPerson = repository.save(person);

            PersonEvent event = new PersonEvent();
            event.setName(savedPerson.getName());
            event.setType("PersonSaved");
            logger.info("Sent event={}", event);
            return event;
        };
    }

    @Bean
    public ListenerContainerCustomizer<AbstractMessageListenerContainer<byte[], byte[]>> customizer() {
        // Disable retry in the AfterRollbackProcessor
        return (container, destination, group) -> container.setAfterRollbackProcessor(
                new DefaultAfterRollbackProcessor<byte[], byte[]>(
                        (record, exception) -> System.out.println("Discarding failed record: " + record),
                        new FixedBackOff(0L, 0)));
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
