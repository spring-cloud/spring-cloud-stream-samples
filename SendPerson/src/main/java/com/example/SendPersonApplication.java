package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@SpringBootApplication
@EnableBinding(Source.class)
public class SendPersonApplication {

	public static void main(String[] args) {
		SpringApplication.run(SendPersonApplication.class, args);

	}

}

@Controller
class SendPersonController {

	private final MessageChannel channel;
	
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String showHome(Model model, @ModelAttribute ("person")Person p) {
		
		Person person = new Person();
		model.addAttribute("person", person);
		
		
		return "home";
	}


	@RequestMapping(value = "/", method = RequestMethod.POST)
	public String write(Person person) {
		this.channel.send(MessageBuilder.withPayload(person).build());
		return "home";
	}

	@Autowired
	public SendPersonController(Source channels) {
		this.channel = channels.output();
	}

}

