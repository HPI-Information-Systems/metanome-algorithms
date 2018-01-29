package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class CastUtilsTest {

	@Test
	public void test() {
		Object obj = "foo";
		String casted = CastUtils.as(obj);
		assertThat(casted).isEqualTo("foo");
	}

	@SuppressWarnings("unused")
	@Test(expected = ClassCastException.class)
	public void testFailedCast() {
		Object obj = "1";
		Integer ignored = CastUtils.<Integer>as(obj);
		fail();
	}

}
