package de.hpi.is.md.util;

import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;

public class Int2Double2ObjectSortedArrayTableTest extends Int2Double2ObjectSortedTableTest {

	@SuppressWarnings("unchecked")
	private static <T> Double2ObjectSortedMap<T>[] toArray(
		Int2ObjectMap<Double2ObjectSortedMap<T>> map, int size) {
		Double2ObjectSortedMap<T>[] array = new Double2ObjectSortedMap[size];
		for (Entry<Double2ObjectSortedMap<T>> entry : map.int2ObjectEntrySet()) {
			int columnKey = entry.getIntKey();
			array[columnKey] = entry.getValue();
		}
		return array;
	}

	@Override
	protected Int2Double2ObjectSortedTable<String> create(
		Int2ObjectMap<Double2ObjectSortedMap<String>> map, int size) {
		Double2ObjectSortedMap<String>[] array = toArray(map, size);
		return new Int2Double2ObjectSortedArrayTable<>(array);
	}

}