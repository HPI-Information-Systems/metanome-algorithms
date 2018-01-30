package de.hpi.is.md.util;

import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class Int2Double2ObjectSortedMapTableTest extends Int2Double2ObjectSortedTableTest {

	@Override
	protected Int2Double2ObjectSortedTable<String> create(
		Int2ObjectMap<Double2ObjectSortedMap<String>> map, int size) {
		return new Int2Double2ObjectSortedMapTable<>(map);
	}

}