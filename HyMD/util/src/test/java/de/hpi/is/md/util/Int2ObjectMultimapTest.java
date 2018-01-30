package de.hpi.is.md.util;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertThat;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public abstract class Int2ObjectMultimapTest {

	private static Map<Integer, Collection<String>> asMap(
		Iterable<Entry<Collection<String>>> multimap) {
		Map<Integer, Collection<String>> map = new HashMap<>();
		Iterator<Entry<Collection<String>>> it = multimap.iterator();
		it.forEachRemaining(e -> map.put(e.getIntKey(), e.getValue()));
		return map;
	}

	@Test
	public void test() {
		Int2ObjectMultimap<String> multimap = create();
		multimap.put(1, "foo");
		multimap.put(2, "bar");
		multimap.put(1, "baz");
		Map<Integer, Collection<String>> map = asMap(multimap);
		Assertions.assertThat(map.size()).isEqualTo(2);
		assertThat(map, hasEntry(is(1), allOf(iterableWithSize(2), hasItems("foo", "baz"))));
		assertThat(map, hasEntry(is(2), allOf(iterableWithSize(1), hasItems("bar"))));
	}

	protected abstract Int2ObjectMultimap<String> create();
}
