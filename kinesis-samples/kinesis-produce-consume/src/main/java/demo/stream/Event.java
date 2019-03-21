/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo.stream;

import java.util.UUID;

import demo.data.Order;

/**
 *
 * @author Peter Oates
 *
 */
public class Event {

	private UUID id;

	private Order subject;

	private String type;

	private String originator;

	public Event() {

	}

	public Event(Order subject, String type, String originator) {
		this.subject = subject;
		this.type = type;
		this.originator = originator;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Order getSubject() {
		return subject;
	}

	public void setSubject(Order subject) {
		this.subject = subject;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOriginator() {
		return originator;
	}

	public void setOriginator(String originator) {
		this.originator = originator;
	}

	@Override
	public String toString() {
		return "Event [id=" + id + ", subject=" + subject + ", type=" + type + ", originator=" + originator + "]";
	}

}
