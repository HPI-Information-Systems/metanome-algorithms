package de.hpi.is.md.hybrid.impl.level;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StatisticsTest {

	@Test
	public void testAdd() {
		Statistics statistics = new Statistics();
		statistics.found();
		statistics.invalid();
		statistics.newDeduced();
		Statistics toAdd = new Statistics();
		toAdd.found();
		toAdd.found();
		toAdd.invalid();
		toAdd.validated();
		toAdd.groupedValidation();
		toAdd.groupedValidation();
		toAdd.round();
		statistics.add(toAdd);
		assertThat(statistics.getRounds()).isEqualTo(1);
		assertThat(statistics.getInvalid()).isEqualTo(2);
		assertThat(statistics.getNewDeduced()).isEqualTo(1);
		assertThat(statistics.getNotSupported()).isEqualTo(0);
		assertThat(statistics.getFound()).isEqualTo(3);
		assertThat(statistics.getValidated()).isEqualTo(1);
		assertThat(statistics.getGroupedValidations()).isEqualTo(2);
	}

	@Test
	public void testFound() {
		Statistics statistics = new Statistics();
		assertThat(statistics.getFound()).isEqualTo(0);
		statistics.found();
		assertThat(statistics.getFound()).isEqualTo(1);
	}

	@Test
	public void testGroupedValidations() {
		Statistics statistics = new Statistics();
		assertThat(statistics.getGroupedValidations()).isEqualTo(0);
		statistics.groupedValidation();
		assertThat(statistics.getGroupedValidations()).isEqualTo(1);
	}

	@Test
	public void testInvalid() {
		Statistics statistics = new Statistics();
		assertThat(statistics.getInvalid()).isEqualTo(0);
		statistics.invalid();
		assertThat(statistics.getInvalid()).isEqualTo(1);
	}

	@Test
	public void testNewDeduced() {
		Statistics statistics = new Statistics();
		assertThat(statistics.getNewDeduced()).isEqualTo(0);
		statistics.newDeduced();
		assertThat(statistics.getNewDeduced()).isEqualTo(1);
	}

	@Test
	public void testNotSupported() {
		Statistics statistics = new Statistics();
		assertThat(statistics.getNotSupported()).isEqualTo(0);
		statistics.notSupported();
		assertThat(statistics.getNotSupported()).isEqualTo(1);
	}

	@Test
	public void testRounds() {
		Statistics statistics = new Statistics();
		assertThat(statistics.getRounds()).isEqualTo(0);
		statistics.round();
		assertThat(statistics.getRounds()).isEqualTo(1);
	}

	@Test
	public void testValidated() {
		Statistics statistics = new Statistics();
		assertThat(statistics.getValidated()).isEqualTo(0);
		statistics.validated();
		assertThat(statistics.getValidated()).isEqualTo(1);
	}

}