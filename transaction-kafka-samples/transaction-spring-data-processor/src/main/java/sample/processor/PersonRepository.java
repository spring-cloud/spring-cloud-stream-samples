package sample.processor;

import org.springframework.data.repository.CrudRepository;

interface PersonRepository extends CrudRepository<Person, String> {

}
