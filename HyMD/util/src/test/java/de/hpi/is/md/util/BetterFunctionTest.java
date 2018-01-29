package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import org.junit.Test;

public class BetterFunctionTest {

	@Test
	public void testThenConsume() {
		Collection<Integer> results = new ArrayList<>();
		BetterFunction<String, Integer> function = String::length;
		Consumer<String> composed = function.thenConsume(results::add);
		composed.accept("foo");
		assertThat(results).hasSize(1);
		assertThat(results).contains(3);
	}

}