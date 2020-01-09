package com.example.kafkanativeserialization;

import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.lang.Nullable;

public class MyJsonSerializer extends JsonSerializer<Object> {

	@Override
	@Nullable
	public byte[] serialize(String topic, Headers headers, @Nullable Object data) {
		return super.serialize(topic, headers, data);
	}
	@Override
	@Nullable
	public byte[] serialize(String topic, @Nullable Object data) {
		return super.serialize(topic, data);
	}
}
