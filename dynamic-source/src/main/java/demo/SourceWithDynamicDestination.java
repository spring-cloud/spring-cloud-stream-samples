/*
 * Copyright 2015 the original author or authors.
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

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.support.Function;
import org.springframework.integration.dsl.support.FunctionExpression;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.integration.router.ExpressionEvaluatingRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Ilayaperumal Gopinathan
 */
@EnableBinding
@Controller
public class SourceWithDynamicDestination {

	@Autowired
	private BinderAwareChannelResolver resolver;

	@Autowired
	@Qualifier("sourceChannel")
	private MessageChannel localChannel;

	@RequestMapping(path = "/", method = POST, consumes = "*/*")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void handleRequest(@RequestBody String body, @RequestHeader(HttpHeaders.CONTENT_TYPE) Object contentType) {
		sendMessage(body, contentType);
	}

	private void sendMessage(Object body, Object contentType) {
		localChannel.send(MessageBuilder.createMessage(body,
				new MessageHeaders(Collections.singletonMap(MessageHeaders.CONTENT_TYPE, contentType))));
	}

	@Bean(name = "sourceChannel")
	public MessageChannel localChannel() {
		return new DirectChannel();
	}

	@Bean
	@ServiceActivator(inputChannel = "sourceChannel")
	public ExpressionEvaluatingRouter router() {
		ExpressionEvaluatingRouter router = new ExpressionEvaluatingRouter(new SpelExpressionParser().parseExpression("payload.id"));
		router.setDefaultOutputChannelName("default-output");
		router.setChannelResolver(resolver);
		return router;
	}
}
