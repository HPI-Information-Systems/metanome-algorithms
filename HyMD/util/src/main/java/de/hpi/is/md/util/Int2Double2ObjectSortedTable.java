package de.hpi.is.md.util;

import java.io.Serializable;
import java.util.Optional;

public interface Int2Double2ObjectSortedTable<V> extends Serializable {

	Optional<V> getCeilingValue(int rowKey, double columnKey);
}
