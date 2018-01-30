package de.hpi.is.md.util;

import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Int2Int2DoubleHashTable implements Int2Int2DoubleTable {

	private static final long serialVersionUID = 6595338101050654631L;
	private final Int2ObjectMap<Int2DoubleRow> map;

	public static Int2Int2DoubleTable create(int height) {
		Int2ObjectOpenHashMap<Int2DoubleRow> map = new Int2ObjectOpenHashMap<>(height);
		return new Int2Int2DoubleHashTable(map);
	}

	@Override
	public double getOrDefault(int rowKey, int columnKey) {
		return row(rowKey).getOrDefault(columnKey);
	}

	@Override
	public void putRow(int rowKey, Int2DoubleRow row) {
		map.put(rowKey, row);
	}

	@Override
	public Int2DoubleRow row(int rowKey) {
		return map.getOrDefault(rowKey, Int2DoubleRow.EMPTY);
	}

	@Override
	public DoubleSet values() {
		ObjectCollection<Int2DoubleRow> values = map.values();
		DoubleSet result = new DoubleOpenHashSet();
		values.stream()
			.map(Int2DoubleRow::values)
			.forEach(result::addAll);
		return result;
	}
}
