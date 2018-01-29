package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import de.hpi.is.md.util.Int2Int2DoubleTable.Int2DoubleRow;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import org.junit.Test;

public abstract class Int2DoubleRowTest {

	@Test
	public void testAsMap() {
		Int2DoubleMap map = new Int2DoubleOpenHashMap();
		map.put(1, 0.5);
		map.put(2, 0.4);
		Int2DoubleRow row = createRow(map, 3);
		assertThat(row.asMap()).hasSize(2);
		assertThat(row.asMap()).containsEntry(1, 0.5);
		assertThat(row.asMap()).containsEntry(2, 0.4);
	}

	@Test
	public void testGetOrDefault() {
		Int2DoubleMap map = new Int2DoubleOpenHashMap();
		map.put(1, 0.5);
		map.put(2, 0.4);
		Int2DoubleRow row = createRow(map, 3);
		assertThat(row.getOrDefault(0)).isEqualTo(0.0);
		assertThat(row.getOrDefault(1)).isEqualTo(0.5);
		assertThat(row.getOrDefault(2)).isEqualTo(0.4);
	}

	@Test
	public void testValues() {
		Int2DoubleMap map = new Int2DoubleOpenHashMap();
		map.put(1, 0.5);
		map.put(2, 0.4);
		Int2DoubleRow row = createRow(map, 3);
		assertThat(row.values()).contains(0.5, 0.4);
	}

	protected abstract Int2DoubleRow createRow(Int2DoubleMap map, int size);

}