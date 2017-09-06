package kinesis.stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.java.Log;

/**
 * 
 * @author Peter Oates
 *
 */
@Log
@Component
public class OrdersSource {

	private OrderProcessor orderOut;

	@Autowired
	public OrdersSource(OrderProcessor orderOut) {
		this.orderOut = orderOut;
	}

	public void sendOrder(Event event) {

		ObjectMapper mapper = new ObjectMapper();
		String eventString = "";
		try {
			eventString = mapper.writeValueAsString(event);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		orderOut.ordersOut().send(MessageBuilder.withPayload(eventString).setHeader("contentType", "application/json")
				.setHeader("aws_partitionKey", "100").build());

		log.info("Event sent: " + eventString);

	}

}
