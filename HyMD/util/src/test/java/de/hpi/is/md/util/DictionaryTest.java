package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import org.junit.Test;

public abstract class DictionaryTest {

	@Test
	public void test() {
		Dictionary<String> dict = createDictionary();
		int value = dict.getOrAdd("foo");
		assertThat(dict.getOrAdd("foo")).isEqualTo(value);
		assertThat(dict.getOrAdd("bar")).isNotEqualTo(value);
	}

	@Test
	public void testSize() {
		Dictionary<String> dict = createDictionary();
		assertThat(dict.size()).isEqualTo(0);
		dict.getOrAdd("foo");
		assertThat(dict.size()).isEqualTo(1);
		dict.getOrAdd("foo");
		assertThat(dict.size()).isEqualTo(1);
		dict.getOrAdd("bar");
		assertThat(dict.size()).isEqualTo(2);
	}

	@Test
	public void testValues() {
		Dictionary<String> dict = createDictionary();
		dict.getOrAdd("foo");
		dict.getOrAdd("bar");
		Collection<String> values = dict.values();
		assertThat(values).hasSize(2);
		assertThat(values).contains("foo");
		assertThat(values).contains("bar");
	}

	protected abstract <T> Dictionary<T> createDictionary();
}
