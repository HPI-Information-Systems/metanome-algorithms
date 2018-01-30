package de.hpi.is.md.util;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class DefaultDictionary<T> extends AbstractDictionary<T> {

	private static final long serialVersionUID = -5333970395893162389L;
	private int nextValue = 0;

	public DefaultDictionary() {
		super(new Object2IntOpenHashMap<>());
	}

	@Override
	public int size() {
		return nextValue;
	}

	@Override
	protected int encode(T value) {
		return nextValue++;
	}
}
