package de.hpi.is.md.util;

import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Int2Double2ObjectSortedMapTable<V> extends AbstractInt2Double2ObjectSortedTable<V> {

	private static final long serialVersionUID = 2019129869920332801L;
	@NonNull
	private final Int2ObjectMap<Double2ObjectSortedMap<V>> map;

	@Override
	protected Optional<Double2ObjectSortedMap<V>> get(int rowKey) {
		Double2ObjectSortedMap<V> row = map.get(rowKey);
		return Optional.ofNullable(row);
	}

}
