package de.hpi.is.md.util.jackson;

import com.google.common.collect.Maps;
import lombok.Data;

@Data
public class Entry<K, V> {

	private final K key;
	private final V value;

	public java.util.Map.Entry<K, V> toEntry() {
		return Maps.immutableEntry(key, value);
	}

}
