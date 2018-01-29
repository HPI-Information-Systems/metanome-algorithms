package de.hpi.is.md.util;

import java.util.Map;

public class LazyMapImpl<K, V> extends BetterMapDecorator<K, V> implements LazyMap<K, V> {

	private static final long serialVersionUID = -4794332794682053652L;
	private final BetterSupplier<V> factory;

	public LazyMapImpl(Map<K, V> map, BetterSupplier<V> factory) {
		super(map);
		this.factory = factory;
	}

	@Override
	public V getOrCreate(K key) {
		return map.computeIfAbsent(key, __ -> factory.get());
	}

}
