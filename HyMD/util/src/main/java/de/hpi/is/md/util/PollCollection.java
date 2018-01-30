package de.hpi.is.md.util;

import java.util.Collection;

public interface PollCollection<T> {

	void add(T value);

	void addAll(Collection<T> values);

	Collection<T> poll();
}
