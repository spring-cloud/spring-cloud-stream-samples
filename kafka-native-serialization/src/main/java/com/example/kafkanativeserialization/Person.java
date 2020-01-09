package com.example.kafkanativeserialization;

public class Person {

	private String name;
	public Person() {
	}
	public Person(String name) {
		this.setName(name);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
