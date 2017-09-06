package kinesis.data;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.ToString;

/**
 * 
 * @author Peter Oates
 *
 */
@ToString
@Entity
public class Order {

	@Id
	private UUID id;

	private String name;

	public Order() {
		id = UUID.randomUUID();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String item) {
		this.name = item;
	}

}
