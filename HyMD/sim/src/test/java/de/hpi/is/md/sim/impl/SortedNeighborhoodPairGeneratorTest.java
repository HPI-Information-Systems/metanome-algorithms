package de.hpi.is.md.sim.impl;

import static org.assertj.core.api.Assertions.assertThat;

import de.hpi.is.md.sim.PairGenerator;
import de.hpi.is.md.util.NullComparator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

public class SortedNeighborhoodPairGeneratorTest {

	@SuppressWarnings("unchecked")
	private static PairGenerator<Integer> createComputer() {
		return new SortedNeighborhoodPairGenerator<>(new NullComparator<>(Integer::compareTo), 3);
	}

	@Test
	public void test() {
		PairGenerator<Integer> generator = createComputer();
		Collection<Tuple2<Integer, Collection<Integer>>> similarities = generator
			.generate(Arrays.asList(1, 2, 3, 6), Arrays.asList(1, 3, 4, 5))
			.getPairs()
			.collect(Collectors.toList());
		assertThat(similarities).hasSize(4);
		assertThat(similarities).contains(new Tuple2<>(1, Arrays.asList(1, 3)));
		assertThat(similarities).contains(new Tuple2<>(2, Arrays.asList(1, 3, 4)));
		assertThat(similarities).contains(new Tuple2<>(3, Arrays.asList(1, 3, 4)));
		assertThat(similarities).contains(new Tuple2<>(6, Collections.singletonList(5)));
	}

	@Test
	public void testNull() {
		PairGenerator<Integer> generator = createComputer();
		Collection<Tuple2<Integer, Collection<Integer>>> similarities = generator
			.generate(Arrays.asList(null, 1, 2, 3), Arrays.asList(null, 1, 2, 3))
			.getPairs()
			.collect(Collectors.toList());
		assertThat(similarities).hasSize(4);
		assertThat(similarities).contains(new Tuple2<>(null, Arrays.asList(null, 1)));
		assertThat(similarities).contains(new Tuple2<>(1, Arrays.asList(null, 1, 2)));
		assertThat(similarities).contains(new Tuple2<>(2, Arrays.asList(1, 2, 3)));
		assertThat(similarities).contains(new Tuple2<>(3, Arrays.asList(2, 3)));
	}

}