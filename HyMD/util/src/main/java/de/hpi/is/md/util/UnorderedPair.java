package de.hpi.is.md.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.Set;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public final class UnorderedPair<T> {

	@NonNull
	private final Set<T> pair;

	public static <T> UnorderedPair<T> of(T value1, T value2) {
		Set<T> set = ImmutableSet.of(value1, value2);
		return new UnorderedPair<>(set);
	}

	public T getFirst() {
		Iterator<T> iterator = pair.iterator();
		return Iterators.get(iterator, 0);
	}

	public T getSecond() {
		//if values are same then there is only one element. Thus, take last element
		int last = pair.size() - 1;
		Iterator<T> iterator = pair.iterator();
		return Iterators.get(iterator, last);
	}

}
