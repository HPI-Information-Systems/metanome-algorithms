package de.hpi.is.md.util;

import java.util.HashMap;

public class LazyMapImplTest extends LazyMapTest {

	@Override
	protected LazyMap<Integer, String> create(BetterSupplier<String> factory, int size) {
		return new LazyMapImpl<>(new HashMap<>(), factory);
	}
}