package de.hpi.is.md.util;

import static de.hpi.is.md.util.CollectionUtils.asList;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;

public class Int2ObjectHashMultimap<T> implements Int2ObjectMultimap<T> {

	private final Int2ObjectMap<Collection<T>> map = new Int2ObjectOpenHashMap<>();

	@Override
	public void put(int key, T value) {
		map.merge(key, asList(value), CollectionUtils::merge);
	}

	@Override
	public Iterator<Entry<Collection<T>>> iterator() {
		Set<Entry<Collection<T>>> entries = entries();
		return entries.iterator();
	}

	@Override
	public Spliterator<Entry<Collection<T>>> spliterator() {
		Set<Entry<Collection<T>>> entries = entries();
		return entries.spliterator();
	}

	private Set<Entry<Collection<T>>> entries() {
		return map.int2ObjectEntrySet();
	}
}
