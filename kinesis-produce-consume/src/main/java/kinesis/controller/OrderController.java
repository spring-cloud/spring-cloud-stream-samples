package kinesis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import kinesis.data.Order;
import kinesis.repository.OrderRepository;
import kinesis.stream.Event;
import kinesis.stream.OrdersSource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 
 * @author Peter Oates
 *
 */
@RestController
public class OrderController {

	    @Autowired 
	    private OrderRepository orders;
	    
	    @Autowired
	    private OrdersSource orderSource;
	    
	    @RequestMapping(value="/orders",method=RequestMethod.GET,produces={"application/json"})
	    @ResponseStatus(HttpStatus.OK)
	    public Iterable<Order> getOrder() {
	 
	    	Iterable<Order> orderList = orders.findAll();
	    	
	    	return orderList;
	    }

		@RequestMapping(method = RequestMethod.POST)
		public ResponseEntity<Order> add(@RequestBody Order input) {

			orders.save(input);
			
			//place order on queue
			orderSource.sendOrder(new Event(input, "ORDER", "KinesisProducer"));
						
			return new ResponseEntity<Order>(input, HttpStatus.OK);
		}

}
