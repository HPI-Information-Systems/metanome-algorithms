package de.hpi.is.md.util;

import java.util.function.BiConsumer;

public interface DoubleObjectBiConsumer<T> extends BiConsumer<Double, T> {

	void accept(double d, T t);

	@Deprecated
	@Override
	default void accept(Double d, T t) {
		accept((double) d, t);
	}

}
