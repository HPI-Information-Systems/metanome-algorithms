package de.hpi.is.md.util;

import java.util.ArrayList;
import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractPollCollection<T> implements PollCollection<T> {

	@NonNull
	private final Collection<T> collection;

	@Override
	public void add(T value) {
		collection.add(value);
	}

	@Override
	public void addAll(Collection<T> values) {
		collection.addAll(values);
	}

	@Override
	public Collection<T> poll() {
		Collection<T> values = new ArrayList<>(collection);
		collection.clear();
		return values;
	}
}
