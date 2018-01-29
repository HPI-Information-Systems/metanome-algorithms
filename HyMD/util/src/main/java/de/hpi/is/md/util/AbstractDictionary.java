package de.hpi.is.md.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractDictionary<T> implements Dictionary<T> {

	private static final long serialVersionUID = -9007173080471006422L;
	@NonNull
	private final Object2IntMap<T> values;

	@Override
	public int getOrAdd(T value) {
		return values.computeIntIfAbsent(value, this::encode);
	}

	@Override
	public Collection<T> values() {
		return values.keySet();
	}

	protected abstract int encode(T value);

}
