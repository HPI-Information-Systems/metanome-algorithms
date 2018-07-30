package de.hpi.is.md.util;

import java.io.Serializable;
import java.util.Collection;

public interface Dictionary<T> extends Serializable {

	int getOrAdd(T value);

	int size();

	Collection<T> values();
}
