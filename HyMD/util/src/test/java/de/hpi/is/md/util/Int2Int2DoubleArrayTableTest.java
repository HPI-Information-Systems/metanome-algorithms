package de.hpi.is.md.util;

public class Int2Int2DoubleArrayTableTest extends Int2Int2DoubleTableTest {

	@Override
	public Int2Int2DoubleTable createTable(int height) {
		return Int2Int2DoubleArrayTable.create(height);
	}

}