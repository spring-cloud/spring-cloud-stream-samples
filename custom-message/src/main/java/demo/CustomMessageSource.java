/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Sokie
 */
@EnableBinding(CustomMessageSource.StreamMessageSource.class)
@Controller
public class CustomMessageSource {

    @Autowired
    private StreamMessageSource channel;

    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    public ResponseEntity sendMessage(@RequestBody CustomMessage customMessage) {
        channel.output().send(new GenericMessage<>(customMessage));
        return new ResponseEntity("", HttpStatus.OK);
    }

    @StreamListener(StreamMessageSource.OUTPUT)
    public synchronized void receive(CustomMessage customMessage) {
        System.out.println("Received custom message: " + customMessage.message + " on queue " + StreamMessageSource.OUTPUT);
    }

    static class CustomMessage{

        private String message;

        public CustomMessage() {
        }

        public CustomMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    interface StreamMessageSource {
        String OUTPUT = "customMessageOutput";

        @Output(OUTPUT)
        MessageChannel output();
    }
}
