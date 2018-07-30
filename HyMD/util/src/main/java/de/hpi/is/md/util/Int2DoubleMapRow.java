package de.hpi.is.md.util;

import static de.hpi.is.md.util.Int2Int2DoubleTable.DEFAULT;

import de.hpi.is.md.util.Int2Int2DoubleTable.Int2DoubleRow;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Int2DoubleMapRow implements Int2DoubleRow {

	private static final long serialVersionUID = -7326716695357407771L;
	private final Int2DoubleMap map;

	@Override
	public DoubleCollection values() {
		return map.values();
	}

	@Override
	public double getOrDefault(int columnKey) {
		return map.getOrDefault(columnKey, DEFAULT);
	}

	@Override
	public Int2DoubleMap asMap() {
		return map;
	}
}
