package de.hpi.is.md.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import java.util.Collection;

public interface Int2ObjectMultimap<T> extends Iterable<Entry<Collection<T>>> {

	void put(int key, T value);
}
