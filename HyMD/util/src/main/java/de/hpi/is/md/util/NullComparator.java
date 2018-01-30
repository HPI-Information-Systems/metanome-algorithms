package de.hpi.is.md.util;

import java.util.Comparator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NullComparator<T> implements Comparator<T> {

	@NonNull
	private final Comparator<T> underlying;

	@Override
	public int compare(T o1, T o2) {
		if (o1 == null) {
			return o2 == null ? 0 : -1;
		}
		if (o2 == null) {
			return 1;
		}
		return underlying.compare(o1, o2);
	}
}
