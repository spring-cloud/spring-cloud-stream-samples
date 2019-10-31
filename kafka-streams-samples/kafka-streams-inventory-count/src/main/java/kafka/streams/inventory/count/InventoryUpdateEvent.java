/*
 * Copyright 2019 the original author or authors.
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
package kafka.streams.inventory.count;

/**
 * @author David Turanski
 */

public class InventoryUpdateEvent {

	public InventoryUpdateEvent() {
		return;
	}

	private int delta;

	private ProductKey key;

	private Action action;

	public enum Action{
		INC, DEC, REP;
	}

	public int getDelta() {
		return delta;
	}

	public void setDelta(int delta) {
		this.delta = delta;
	}

	public ProductKey getKey() {
		return key;
	}

	public void setKey(ProductKey key) {
		this.key = key;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}
}
