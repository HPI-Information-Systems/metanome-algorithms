package de.hpi.is.md.util;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Optionals {

	public static <T> Optional<Collection<T>> of(Collection<T> collection) {
		return collection.isEmpty() ? Optional.empty() : Optional.of(collection);
	}

	public static <T> Stream<T> stream(Optional<T> optional) {
		return optional.map(Stream::of)
			.orElseGet(Stream::empty);
	}

}
