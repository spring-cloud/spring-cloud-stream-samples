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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.handler.annotation.SendTo;

@EnableBinding(Processor.class)
public class RxJavaTransformer {

	private static Logger logger = LoggerFactory.getLogger(RxJavaTransformer.class);

	@StreamListener(Processor.INPUT)
	@SendTo(Processor.OUTPUT)
	public Observable<String> processor(Observable<String> inputStream) {
		return inputStream.map(data -> {
			logger.info("Got data = " + data);
			return data;
		}).buffer(5).map(data -> String.valueOf(avg(data)));
	}

	private static Double avg(List<String> data) {
		double sum = 0;
		double count = 0;
		for(String d : data) {
			count++;
			sum += Double.valueOf(d);
		}
		return sum/count;
	}

}
