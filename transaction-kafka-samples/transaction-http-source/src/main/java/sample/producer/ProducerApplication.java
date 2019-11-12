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
package sample.producer;

import java.util.StringJoiner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Bjarte Stien Karlsen
 */
@SpringBootApplication
@RestController
public class ProducerApplication {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    BlockingQueue<PersonEvent> unbounded = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }

    @PostMapping()
    public String sendMessage(@RequestBody PersonEvent incomming) {
        PersonEvent personEvent = new PersonEvent();
        personEvent.setType("CreatePerson");
        personEvent.setName(incomming.getName());
        unbounded.offer(personEvent);
        logger.info("Person sent={}", personEvent);
        return "Person sent";
    }

    @Bean
    public Supplier<PersonEvent> supplier() {
        return () -> unbounded.poll();
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

