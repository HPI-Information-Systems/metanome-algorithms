package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import org.junit.Test;

public abstract class Int2Double2ObjectSortedTableTest {

	@Test
	public void test() {
		Double2ObjectSortedMap<String> row1 = new Double2ObjectRBTreeMap<>();
		row1.put(0.5, "foo");
		row1.put(0.6, "bar");
		Int2ObjectMap<Double2ObjectSortedMap<String>> map = Int2ObjectMaps.singleton(1, row1);
		Int2Double2ObjectSortedTable<String> table = create(map, 3);
		assertThat(table.getCeilingValue(1, 0.5)).hasValue("foo");
		assertThat(table.getCeilingValue(1, 0.55)).hasValue("bar");
		assertThat(table.getCeilingValue(1, 0.7)).isEmpty();
		assertThat(table.getCeilingValue(2, 0.5)).isEmpty();
	}

	protected abstract Int2Double2ObjectSortedTable<String> create(
		Int2ObjectMap<Double2ObjectSortedMap<String>> map, int size);
}
