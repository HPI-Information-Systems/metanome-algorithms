package de.hpi.is.md.util;

import static de.hpi.is.md.util.Int2Int2DoubleTable.DEFAULT;

import de.hpi.is.md.util.Int2Int2DoubleTable.Int2DoubleRow;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Int2DoubleArrayRow implements Int2DoubleRow {

	private static final long serialVersionUID = -120961428097253882L;
	private final double[] array;

	private static boolean isNotDefault(double d) {
		return d != DEFAULT;
	}

	@Override
	public DoubleCollection values() {
		DoubleCollection values = new DoubleOpenHashSet();
		Arrays.stream(array)
			.filter(Int2DoubleArrayRow::isNotDefault)
			.forEach(values::add);
		return values;
	}

	@Override
	public double getOrDefault(int columnKey) {
		return array[columnKey];
	}

	@Override
	public Int2DoubleMap asMap() {
		Int2DoubleMap mapRow = new Int2DoubleOpenHashMap(array.length);
		for (int i = 0; i < array.length; i++) {
			double value = array[i];
			if (isNotDefault(value)) {
				mapRow.put(i, value);
			}
		}
		return mapRow;
	}
}
