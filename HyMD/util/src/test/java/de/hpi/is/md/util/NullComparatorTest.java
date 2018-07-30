package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import org.junit.Test;

public class NullComparatorTest {

	@Test
	public void test() {
		Comparator<Integer> comparator = new NullComparator<>(Integer::compare);
		assertThat(comparator.compare(1, 2)).isEqualTo(Integer.compare(1, 2));
		assertThat(comparator.compare(null, null)).isEqualTo(0);
		assertThat(comparator.compare(null, 1)).isLessThan(0);
		assertThat(comparator.compare(1, null)).isGreaterThan(0);
	}

}