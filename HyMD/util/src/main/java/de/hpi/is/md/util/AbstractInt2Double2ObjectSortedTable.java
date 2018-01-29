package de.hpi.is.md.util;

import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

public abstract class AbstractInt2Double2ObjectSortedTable<V> implements
	Int2Double2ObjectSortedTable<V> {

	private static final long serialVersionUID = 6700402286880907145L;

	@Override
	public Optional<V> getCeilingValue(int rowKey, double columnKey) {
		return with(columnKey).getCeilingValue(rowKey);
	}

	protected abstract Optional<Double2ObjectSortedMap<V>> get(int rowKey);

	private WithColumn with(double columnKey) {
		return new WithColumn(columnKey);
	}

	@RequiredArgsConstructor
	private class WithColumn {

		private final double columnKey;

		private Optional<V> getCeilingValue(int rowKey) {
			return get(rowKey).flatMap(this::getCeilingValue);
		}

		private Optional<V> getCeilingValue(Double2ObjectSortedMap<V> row) {
			return CollectionUtils.ceilingValue(row, columnKey);
		}
	}
}
