package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void testToLowerCase() {
		assertThat(StringUtils.toLowerCase("foO")).isEqualTo("foo");
		assertThat(StringUtils.toLowerCase(null)).isNull();
	}

}