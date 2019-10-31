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

import java.util.Objects;

/**
 * @author David Turanski
 */
public class InventoryCountEvent {

	private int count;

	private ProductKey key;


	public InventoryCountEvent(){
	};


	public InventoryCountEvent(ProductKey key, int count) {
		this.count = count;
		this.key = key;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public ProductKey getKey() {
		return key;
	}

	public void setKey(ProductKey key) {
		this.key = key;
	}



	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InventoryCountEvent that = (InventoryCountEvent) o;
		return count == that.count &&
				Objects.equals(key, that.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(count, key);
	}
}
