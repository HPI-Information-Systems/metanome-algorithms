package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

@SuppressWarnings("ConstantConditions")
public class ObjectUtilsTest {

	@Test
	public void testBothNull() {
		assertThat(ObjectUtils.bothNull(null, null)).isTrue();
		assertThat(ObjectUtils.bothNull("", null)).isFalse();
		assertThat(ObjectUtils.bothNull(null, "")).isFalse();
		assertThat(ObjectUtils.bothNull("", "")).isFalse();
	}

	@Test
	public void testEitherNull() {
		assertThat(ObjectUtils.eitherNull(null, null)).isTrue();
		assertThat(ObjectUtils.eitherNull("", null)).isTrue();
		assertThat(ObjectUtils.eitherNull(null, "")).isTrue();
		assertThat(ObjectUtils.eitherNull("", "")).isFalse();
	}

}