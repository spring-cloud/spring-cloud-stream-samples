/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import demo.data.Order;
import demo.repository.OrderRepository;
import demo.stream.Event;
import demo.stream.OrdersSource;

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

	@RequestMapping(value = "/orders", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseStatus(HttpStatus.OK)
	public Iterable<Order> getOrder() {

		Iterable<Order> orderList = orders.findAll();

		return orderList;
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Order> add(@RequestBody Order input) {

		orders.save(input);

		// place order on queue
		orderSource.sendOrder(new Event(input, "ORDER", "KinesisProducer"));

		return new ResponseEntity<Order>(input, HttpStatus.OK);
	}

}
