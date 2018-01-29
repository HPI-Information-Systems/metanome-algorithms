package de.hpi.is.md.util;

import static org.junit.Assert.fail;

import org.junit.Test;

public class LazyArrayTest extends LazyMapTest {

	@Test(expected = IndexOutOfBoundsException.class)
	public void testGetElementOutOfBounds() {
		LazyMap<Integer, String> array = create(() -> null, 3);
		array.get(4);
		fail();
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testGetOrCreateElementOutOfBounds() {
		LazyMap<Integer, String> array = create(() -> null, 3);
		array.getOrCreate(4);
		fail();
	}

	@Override
	protected LazyMap<Integer, String> create(BetterSupplier<String> factory, int size) {
		return new LazyArray<>(new String[size], factory);
	}

}