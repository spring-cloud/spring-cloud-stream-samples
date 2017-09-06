package kinesis.stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import kinesis.repository.OrderRepository;

import lombok.extern.java.Log;

/**
 * 
 * @author Peter Oates
 *
 */
@Log
@EnableBinding(OrderProcessor.class)
public class OrderStreamConfiguration {

    @Autowired 
    private OrderRepository orders;
	
	  @StreamListener(OrderProcessor.INPUT)
	  public void processOrder(Event event) {

		  //log the order received
		  if (!event.getOriginator().equals("KinesisProducer")) { 
			  log.info("An order has been received " + event.toString());
			  
			  orders.save(event.getSubject());
		  } else {
			  log.info("An order has been placed from this service " + event.toString());
		  }

		  
	  }
	
}
