package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class IntegerObjectBiConsumerTest {

	@SuppressWarnings("deprecation")
	@Test
	public void test() {
		Map<Integer, String> map = new HashMap<>();
		IntObjectBiConsumer<String> consumer = map::put;
		consumer.accept(Integer.valueOf(1), "foo");
		assertThat(map).hasSize(1);
		assertThat(map).containsEntry(1, "foo");
	}

}