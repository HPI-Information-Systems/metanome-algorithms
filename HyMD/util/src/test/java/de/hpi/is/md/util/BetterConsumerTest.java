package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import org.junit.Test;

public class BetterConsumerTest {

	@Test
	public void testCompose() {
		Collection<Integer> results = new ArrayList<>();
		BetterConsumer<Integer> consumer = results::add;
		Consumer<String> composed = consumer.compose(String::length);
		composed.accept("foo");
		assertThat(results).hasSize(1);
		assertThat(results).contains(3);
	}

}