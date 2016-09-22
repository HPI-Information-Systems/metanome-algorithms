package de.hpi.mpss2015n.approxind.utils;

import java.util.LinkedHashMap;

/**
 * Key-value cache with LRU semantics.
 */
public class AOCacheMap<K, V> extends LinkedHashMap<K, V>{

	private int maxCapacity;

	public AOCacheMap(int maxCapacity) {
		super(maxCapacity,0.7f, true);
		this.maxCapacity=maxCapacity;
	}
	
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		return size()>maxCapacity;
	}
}
