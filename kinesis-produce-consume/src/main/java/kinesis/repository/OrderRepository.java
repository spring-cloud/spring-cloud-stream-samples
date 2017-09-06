package kinesis.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import kinesis.data.Order;

/**
 * 
 * @author Peter Oates
 *
 */
@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {

}
