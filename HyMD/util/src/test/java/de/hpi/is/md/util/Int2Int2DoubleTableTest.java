package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMaps;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import org.junit.Test;

public abstract class Int2Int2DoubleTableTest {

	@Test
	public void testGetOrDefault() {
		Int2Int2DoubleTable table = createTable(3);
		assertThat(table.getOrDefault(1, 1)).isEqualTo(0.0);
		assertThat(table.getOrDefault(1, 2)).isEqualTo(0.0);
		assertThat(table.getOrDefault(2, 1)).isEqualTo(0.0);
		table.putRow(1, new Int2DoubleMapRow(Int2DoubleMaps.singleton(1, 1.0)));
		assertThat(table.getOrDefault(1, 1)).isEqualTo(1.0);
		assertThat(table.getOrDefault(1, 2)).isEqualTo(0.0);
		assertThat(table.getOrDefault(2, 1)).isEqualTo(0.0);
		table.putRow(1, new Int2DoubleMapRow(Int2DoubleMaps.singleton(1, 3.0)));
		assertThat(table.getOrDefault(1, 1)).isEqualTo(3.0);
		assertThat(table.getOrDefault(1, 2)).isEqualTo(0.0);
		assertThat(table.getOrDefault(2, 1)).isEqualTo(0.0);
	}

	@Test
	public void testRow() {
		Int2Int2DoubleTable table = createTable(3);
		assertThat(table.row(1).asMap()).hasSize(0);
		assertThat(table.row(2).asMap()).hasSize(0);
		table.putRow(1, new Int2DoubleMapRow(Int2DoubleMaps.singleton(1, 1.0)));
		assertThat(table.row(1).asMap()).hasSize(1);
		assertThat(table.row(1).asMap()).containsEntry(1, 1.0);
		assertThat(table.row(2).asMap()).hasSize(0);
		table.putRow(1, new Int2DoubleMapRow(Int2DoubleMaps.singleton(1, 3.0)));
		assertThat(table.row(1).asMap()).hasSize(1);
		assertThat(table.row(1).asMap()).containsEntry(1, 3.0);
		assertThat(table.row(2).asMap()).hasSize(0);
		table.putRow(1,
			new Int2DoubleMapRow(new Int2DoubleOpenHashMap(ImmutableMap.of(1, 3.0, 2, 2.0))));
		assertThat(table.row(1).asMap()).hasSize(2);
		assertThat(table.row(1).asMap()).containsEntry(1, 3.0);
		assertThat(table.row(1).asMap()).containsEntry(2, 2.0);
		assertThat(table.row(2).asMap()).hasSize(0);
		table.putRow(2, new Int2DoubleMapRow(Int2DoubleMaps.singleton(1, 2.0)));
		assertThat(table.row(1).asMap()).hasSize(2);
		assertThat(table.row(1).asMap()).containsEntry(1, 3.0);
		assertThat(table.row(1).asMap()).containsEntry(2, 2.0);
		assertThat(table.row(2).asMap()).hasSize(1);
		assertThat(table.row(2).asMap()).containsEntry(1, 2.0);
	}

	@Test
	public void values() {
		Int2Int2DoubleTable table = createTable(3);
		assertThat(table.values()).isEmpty();
		table.putRow(1, new Int2DoubleMapRow(Int2DoubleMaps.singleton(1, 1.0)));
		assertThat(table.values()).hasSize(1);
		assertThat(table.values()).contains(1.0);
		table.putRow(1, new Int2DoubleMapRow(Int2DoubleMaps.singleton(1, 2.0)));
		assertThat(table.values()).hasSize(1);
		assertThat(table.values()).contains(2.0);
		table.putRow(1,
			new Int2DoubleMapRow(new Int2DoubleOpenHashMap(ImmutableMap.of(1, 2.0, 2, 2.0))));
		assertThat(table.values()).hasSize(1);
		assertThat(table.values()).contains(2.0);
		table.putRow(2, new Int2DoubleMapRow(Int2DoubleMaps.singleton(1, 3.0)));
		assertThat(table.values()).hasSize(2);
		assertThat(table.values()).contains(2.0, 3.0);
	}

	protected abstract Int2Int2DoubleTable createTable(int height);
}
