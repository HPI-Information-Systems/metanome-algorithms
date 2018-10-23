package de.metanome.algorithms.binder.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple LRU cache implementation.
 */
public class LruCache<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 2109386626216996633L;
	
	private int capacity;

    public LruCache(int capacity) {
        super(capacity);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return this.size() > this.capacity;
    }
}
