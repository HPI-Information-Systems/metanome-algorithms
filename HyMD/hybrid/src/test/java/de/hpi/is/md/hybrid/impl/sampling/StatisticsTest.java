package de.hpi.is.md.hybrid.impl.sampling;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StatisticsTest {

	@Test
	public void testAdd() {
		Statistics statistics = new Statistics();
		statistics.count();
		Statistics toAdd = new Statistics();
		toAdd.count();
		toAdd.newDeduced();
		statistics.add(toAdd);
		assertThat(statistics.getCount()).isEqualTo(2);
		assertThat(statistics.getNewDeduced()).isEqualTo(1);
	}

	@Test
	public void testCount() {
		Statistics statistics = new Statistics();
		assertThat(statistics.getCount()).isEqualTo(0);
		statistics.count();
		assertThat(statistics.getCount()).isEqualTo(1);
	}

	@Test
	public void testNewDeduced() {
		Statistics statistics = new Statistics();
		assertThat(statistics.getNewDeduced()).isEqualTo(0);
		statistics.newDeduced();
		assertThat(statistics.getNewDeduced()).isEqualTo(1);
	}

}