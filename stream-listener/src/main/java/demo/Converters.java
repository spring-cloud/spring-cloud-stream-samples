/*
 * Copyright 2016 the original author or authors.
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

package demo;

import org.springframework.cloud.stream.converter.AbstractFromMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.util.MimeType;

/**
 * @author Ilayaperumal Gopinathan
 */
@Configuration
public class Converters {

	//Register custom converter
	@Bean
	public AbstractFromMessageConverter fooConverter() {
		return new FooToBarConverter();
	}

	public static class Foo {

		private String value = "foo";

		public String getValue() {
			return this.value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static class Bar {

		private String value = "init";

		public Bar(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

	public static class FooToBarConverter extends AbstractFromMessageConverter {

		public FooToBarConverter() {
			super(MimeType.valueOf("test/bar"));
		}

		@Override
		protected Class<?>[] supportedTargetTypes() {
			return new Class[] {Bar.class};
		}

		@Override
		protected Class<?>[] supportedPayloadTypes() {
			return new Class<?>[] {Foo.class};
		}

		@Override
		public Object convertFromInternal(Message<?> message, Class<?> targetClass, Object conversionHint) {
			Object result = null;
			try {
				if (message.getPayload() instanceof Foo) {
					Foo fooPayload = (Foo) message.getPayload();
					result = new Bar(fooPayload.getValue());
				}
			}
			catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new MessageConversionException(e.getMessage());
			}
			return result;
		}
	}
}
