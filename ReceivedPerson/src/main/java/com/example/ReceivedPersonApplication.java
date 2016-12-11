package com.example;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
@EnableBinding(Sink.class)
public class ReceivedPersonApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReceivedPersonApplication.class, args);
	}
	
	@Autowired
	ReceivedPersonRepository personR;
	
	
	
	@StreamListener(value = Sink.INPUT)
	public void logMessages(ReceivedPerson person){
		ReceivedPerson p = new ReceivedPerson(person.getEmail(), person.getName());
		personR.save(p);
		List<ReceivedPerson> personList = personR.findAll();
		System.out.println(personList + System.lineSeparator());
		
	}

	@RequestMapping(path = "/", method = RequestMethod.GET)
	public List<ReceivedPerson> getReceived(){
		
		List<ReceivedPerson> person = personR.findAll();
		
		return person;
	}
	
	@RequestMapping(path = "/find/{id}", method = RequestMethod.GET)
	public ReceivedPerson findReceived(@PathVariable String id){
		
		ReceivedPerson person = personR.findById(id);
		
		return person;
	}
	
	@RequestMapping(path = "/delete/{id}", method = RequestMethod.GET)
	public void deleteReceived(@PathVariable String id){
		
		ReceivedPerson person = personR.findById(id);
		personR.delete(person);
		
	}
	
}

interface ReceivedPersonRepository extends JpaRepository<ReceivedPerson, Long>{
	
	ReceivedPerson findById(String id);

	
}

