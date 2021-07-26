package demo;

import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;

import java.util.Arrays;
import java.util.Optional;

/**
 * A custom implementation of {@link MessageRoutingCallback} that will route each message to its corresponding binding channel.
 * This is the equivalence of {@link StreamListener#condition()}
 */
public class CustomMessageRoutingCallback implements MessageRoutingCallback {

	public static final String EVENT_TYPE = "event_type";

	@Override
	public String functionDefinition(Message<?> message) {
		return Optional.of(message.getHeaders())
				.filter(headers -> headers.containsKey(EVENT_TYPE))
				.map(messageHeaders -> messageHeaders.get(EVENT_TYPE))
				.map(eventType -> EventTypeToBinding.valueOf((String)eventType))
				.map(EventTypeToBinding::getBinding)
				.orElseThrow(() -> new IllegalStateException("event_type was not recognized !! supported values are " + Arrays.toString(EventTypeToBinding.values())));
	}
}

