package de.hpi.is.md.util;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;

public interface BetterFunction<T, R> extends Function<T, R>, Serializable {

	default Consumer<T> thenConsume(Consumer<R> consumer) {
		return obj -> consumer.accept(apply(obj));
	}

}
