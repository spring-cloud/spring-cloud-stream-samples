package kinesis.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * 
 * @author Peter Oates
 *
 */
public interface OrderProcessor {

	String INPUT = "ordersIn";
	
	@Output
	MessageChannel ordersOut();
	
	@Input
	SubscribableChannel ordersIn();

}
