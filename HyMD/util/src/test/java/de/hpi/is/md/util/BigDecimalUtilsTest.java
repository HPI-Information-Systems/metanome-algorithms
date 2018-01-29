package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class BigDecimalUtilsTest {

	@Test
	public void test() {
		assertThat(BigDecimalUtils.valueOf(0).intValue()).isEqualTo(0);
		assertThat(BigDecimalUtils.valueOf(100).intValue()).isEqualTo(100);
		assertThat(BigDecimalUtils.valueOf(1000).intValue()).isEqualTo(1000);
	}

}