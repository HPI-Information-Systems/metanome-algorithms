package de.hpi.is.md.util;

import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMaps;
import java.io.Serializable;

public interface Int2Int2DoubleTable extends Serializable {

	double DEFAULT = 0.0;

	double getOrDefault(int rowKey, int columnKey);

	void putRow(int rowKey, Int2DoubleRow row);

	Int2DoubleRow row(int rowKey);

	DoubleSet values();

	interface Int2DoubleRow extends Serializable {

		Int2DoubleRow EMPTY = new Int2DoubleMapRow(Int2DoubleMaps.EMPTY_MAP);

		Int2DoubleMap asMap();

		double getOrDefault(int columnKey);

		DoubleCollection values();
	}
}
