package de.hpi.is.md.relational;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ColumnPairTest {

	@Test
	public void test() {
		Column<Integer> left = Column.of("a", Integer.class);
		Column<Integer> right = Column.of("a", Integer.class);
		ColumnPair<Integer> pair = new ColumnPair<>(left, right);
		assertThat(pair.getType()).isEqualTo(Integer.class);
		assertThat(pair.getType()).isEqualTo(left.getType());
		assertThat(pair.getType()).isEqualTo(right.getType());
	}

}