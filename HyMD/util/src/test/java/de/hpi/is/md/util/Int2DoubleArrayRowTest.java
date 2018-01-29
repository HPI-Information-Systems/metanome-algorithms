package de.hpi.is.md.util;

import de.hpi.is.md.util.Int2Int2DoubleTable.Int2DoubleRow;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap.Entry;

public class Int2DoubleArrayRowTest extends Int2DoubleRowTest {

	@Override
	protected Int2DoubleRow createRow(Int2DoubleMap map, int size) {
		double[] row = new double[size];
		for (Entry entry : map.int2DoubleEntrySet()) {
			int columnKey = entry.getIntKey();
			row[columnKey] = entry.getDoubleValue();
		}
		return new Int2DoubleArrayRow(row);
	}
}
