package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.junit.Test;

public abstract class LazyMapTest {

	@Test
	public void testForEach() {
		Iterator<String> it = Arrays.asList("foo", "bar").iterator();
		BetterSupplier<String> factory = it::next;
		int size = 3;
		LazyMap<Integer, String> array = create(factory, size);
		array.getOrCreate(0);
		array.getOrCreate(2);
		Map<Integer, String> indexes = new HashMap<>();
		array.forEach(indexes::put);
		assertThat(indexes).hasSize(2);
		assertThat(indexes).containsEntry(0, "foo");
		assertThat(indexes).containsEntry(2, "bar");
	}

	@Test
	public void testGetElement() {
		LazyMap<Integer, String> array = create(() -> null, 3);
		assertThat(array.get(0)).isEmpty();
		assertThat(array.get(1)).isEmpty();
		assertThat(array.get(2)).isEmpty();
	}

	@Test
	public void testGetOrCreateElement() {
		Iterator<String> it = Arrays.asList("foo", "bar").iterator();
		LazyMap<Integer, String> array = create(it::next, 3);
		assertThat(array.get(0).isPresent()).isFalse();
		assertThat(array.getOrCreate(0)).isEqualTo("foo");
		assertThat(array.get(0)).hasValue("foo");
		assertThat(array.getOrCreate(1)).isEqualTo("bar");
		assertThat(array.getOrCreate(0)).isEqualTo("foo");
	}

	protected abstract LazyMap<Integer, String> create(BetterSupplier<String> factory, int size);
}
