package de.hpi.is.md.util;

import java.util.function.BiConsumer;

public interface IntObjectBiConsumer<T> extends BiConsumer<Integer, T> {

	void accept(int i, T t);

	@Deprecated
	@Override
	default void accept(Integer i, T t) {
		accept(i.intValue(), t);
	}
}
