package de.hpi.is.md.util;

public interface LazyMap<K, V> extends BetterMap<K, V> {

	V getOrCreate(K key);
}
