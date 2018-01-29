package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class DoubleObjectBiConsumerTest {

	@SuppressWarnings("deprecation")
	@Test
	public void test() {
		Map<Double, String> map = new HashMap<>();
		DoubleObjectBiConsumer<String> consumer = map::put;
		consumer.accept(Double.valueOf(1.0), "foo");
		assertThat(map).hasSize(1);
		assertThat(map).containsEntry(1.0, "foo");
	}

}