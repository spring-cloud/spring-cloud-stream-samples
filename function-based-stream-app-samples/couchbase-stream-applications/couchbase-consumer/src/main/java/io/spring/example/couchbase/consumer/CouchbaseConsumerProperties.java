/*
 * Copyright 2020-2020 the original author or authors.
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

package io.spring.example.couchbase.consumer;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("couchbase.consumer")
@Validated
public class CouchbaseConsumerProperties {

	private static final String DEFAULT_VALUE_EXPRESSION = "payload";

	private final SpelExpressionParser parser = new SpelExpressionParser();

	/**
	 * A SpEL expression to specify the bucket.
	 */
	private Expression bucketExpression;

	/**
	 * A SpEL expression to specify the key.
	 */
	private Expression keyExpression;

	/**
	 * A SpEL expression to specify the collection.
	 */
	private Expression collectionExpression;

	/**
	 * A SpEL expression to specify the value (default is payload).
	 */
	private Expression valueExpression = parser.parseExpression(DEFAULT_VALUE_EXPRESSION);

	@NotNull(message = "'valueExpression' is required")
	public Expression getValueExpression() {
		return valueExpression;
	}

	public void setValueExpression(Expression valueExpression) {
		this.valueExpression = valueExpression;
	}

	public Expression getCollectionExpression() {
		return collectionExpression;
	}

	public void setCollectionExpression(Expression collectionExpression) {
		this.collectionExpression = collectionExpression;
	}

	@NotNull(message = "'keyExpression' is required")
	public Expression getKeyExpression() {
		return keyExpression;
	}

	public void setKeyExpression(Expression keyExpression) {
		this.keyExpression = keyExpression;
	}

	@NotNull(message = "'bucketExpression' is required")
	public Expression getBucketExpression() {
		return bucketExpression;
	}

	public void setBucketExpression(Expression bucketExpression) {
		this.bucketExpression = bucketExpression;
	}
}
