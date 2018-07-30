package de.hpi.is.md.util;

import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Int2Int2DoubleArrayTable implements Int2Int2DoubleTable {

	private static final long serialVersionUID = 6907741376133014647L;
	@NonNull
	private final Int2DoubleRow[] array;

	public static Int2Int2DoubleTable create(int height) {
		Int2DoubleRow[] array = new Int2DoubleRow[height];
		return new Int2Int2DoubleArrayTable(array);
	}

	@Override
	public double getOrDefault(int rowKey, int columnKey) {
		Int2DoubleRow row = array[rowKey];
		return row == null ? DEFAULT : row.getOrDefault(columnKey);
	}

	@Override
	public void putRow(int rowKey, Int2DoubleRow row) {
		array[rowKey] = row;
	}

	@Override
	public Int2DoubleRow row(int rowKey) {
		return Optional.ofNullable(array[rowKey])
			.orElse(Int2DoubleRow.EMPTY);
	}

	@Override
	public DoubleSet values() {
		DoubleSet result = new DoubleOpenHashSet();
		Arrays.stream(array)
			.filter(Objects::nonNull)
			.map(Int2DoubleRow::values)
			.flatMap(DoubleCollection::stream)
			.mapToDouble(Double::doubleValue)
			.forEach(result::add);
		return result;
	}
}
