package de.hpi.is.md.util;

import java.io.Serializable;
import java.util.function.Consumer;

public interface BetterConsumer<T> extends Consumer<T>, Serializable {

	default <U> Consumer<U> compose(BetterFunction<U, T> function) {
		return function.thenConsume(this);
	}
}
