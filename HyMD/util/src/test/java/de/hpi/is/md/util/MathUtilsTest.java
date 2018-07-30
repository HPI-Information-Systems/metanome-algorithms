package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MathUtilsTest {

	@Test
	public void testDivide() {
		assertThat(MathUtils.divide(8.0, 2.0)).isEqualTo(4.0);
		assertThat(MathUtils.divide(0.0, 2.0)).isEqualTo(0.0);
		assertThat(MathUtils.divide(8.0, 0.0)).isEqualTo(0.0);
	}

	@Test
	public void testIncrement() {
		assertThat(MathUtils.increment(8L)).isEqualTo(9L);
		assertThat(MathUtils.increment(0L)).isEqualTo(1L);
		assertThat(MathUtils.increment(-8L)).isEqualTo(-7L);
	}

	@Test
	public void testMultiply() {
		assertThat(MathUtils.multiply(8L, 2L)).isEqualTo(16L);
		assertThat(MathUtils.multiply(0L, 2L)).isEqualTo(0L);
		assertThat(MathUtils.multiply(8L, 0L)).isEqualTo(0L);
	}
}