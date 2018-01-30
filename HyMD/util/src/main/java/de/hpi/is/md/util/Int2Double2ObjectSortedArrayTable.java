package de.hpi.is.md.util;

import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Int2Double2ObjectSortedArrayTable<V> extends AbstractInt2Double2ObjectSortedTable<V> {

	private static final long serialVersionUID = 3705813803498950508L;
	@NonNull
	private final Double2ObjectSortedMap<V>[] array;

	@Override
	protected Optional<Double2ObjectSortedMap<V>> get(int rowKey) {
		Double2ObjectSortedMap<V> row = array[rowKey];
		return Optional.ofNullable(row);
	}

}
