package de.hpi.is.md.util;

import de.hpi.is.md.util.Int2Int2DoubleTable.Int2DoubleRow;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

public class Int2DoubleMapRowTest extends Int2DoubleRowTest {

	@Override
	protected Int2DoubleRow createRow(Int2DoubleMap map, int size) {
		return new Int2DoubleMapRow(map);
	}
}
