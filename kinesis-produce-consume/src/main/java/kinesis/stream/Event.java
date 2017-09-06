package kinesis.stream;

import java.util.UUID;

import kinesis.data.Order;

import lombok.Data;
import lombok.ToString;

/**
 * 
 * @author Peter Oates
 *
 */
@Data
@ToString
public class Event {

    private UUID id;
	private Order subject;
	private String type;
	private String originator;
	
	public Event() {
		
	}
	
	public Event(Order subject, String type, String originator) {
		this.subject =  subject;
		this.type = type;
		this.originator = originator;
	}

}
