package de.hpi.is.md.util;

import java.util.Iterator;
import java.util.Optional;
import java.util.PrimitiveIterator.OfDouble;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IteratorUtils {

	public static <T> Optional<T> next(Iterator<T> it) {
		if (it.hasNext()) {
			T entry = it.next();
			return Optional.of(entry);
		}
		return Optional.empty();
	}

	public static OptionalDouble next(OfDouble it) {
		if (it.hasNext()) {
			double higher = it.nextDouble();
			return OptionalDouble.of(higher);
		}
		return OptionalDouble.empty();
	}
}
