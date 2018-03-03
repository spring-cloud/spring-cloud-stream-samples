/*
 * Copyright 2015-2018 the original author or authors.
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

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author Ilayaperumal Gopinathan
 */
@EnableBinding(SampleSink.MultiInputSink.class)
public class SampleSink {

	@StreamListener(MultiInputSink.INPUT1)
	public synchronized void receive1(String message) {
		System.out.println("******************");
		System.out.println("At Sink1");
		System.out.println("******************");
		System.out.println("Received message " + message);
	}

	@StreamListener(MultiInputSink.INPUT2)
	public synchronized void receive2(String message) {
		System.out.println("******************");
		System.out.println("At Sink2");
		System.out.println("******************");
		System.out.println("Received message " + message);
	}

	public interface MultiInputSink {
		String INPUT1 = "input1";

		String INPUT2 = "input2";

		@Input(INPUT1)
		SubscribableChannel input1();

		@Input(INPUT2)
		SubscribableChannel input2();
	}
}
