package com.example.kafkanativeserialization;

import org.springframework.kafka.support.serializer.JsonDeserializer;

public class MyJsonDeserializer extends JsonDeserializer<Person> {
}
