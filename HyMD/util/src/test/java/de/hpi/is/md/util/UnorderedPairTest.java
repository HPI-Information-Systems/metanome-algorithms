package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;

import org.junit.Assert;
import org.junit.Test;

public class UnorderedPairTest {

	@Test
	public void testEquality() {
		UnorderedPair<Integer> pair1 = UnorderedPair.of(1, 2);
		UnorderedPair<Integer> pair2 = UnorderedPair.of(2, 1);
		assertThat(pair1).isEqualTo(pair2);
	}

	@Test
	public void testFirstAndSecond() {
		UnorderedPair<Integer> pair = UnorderedPair.of(1, 2);
		int first = pair.getFirst();
		int second = pair.getSecond();
		assertThat(first).isNotEqualTo(second);
		Assert.assertThat(first, either(is(1)).or(is(2)));
		Assert.assertThat(second, either(is(1)).or(is(2)));
	}

	@Test
	public void testNotEquals() {
		assertThat(UnorderedPair.of(1, 2)).isNotEqualTo(UnorderedPair.of(1, 3));
	}

}
