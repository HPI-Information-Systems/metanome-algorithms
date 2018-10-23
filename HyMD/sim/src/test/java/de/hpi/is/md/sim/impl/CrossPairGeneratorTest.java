package de.hpi.is.md.sim.impl;

import static org.assertj.core.api.Assertions.assertThat;

import de.hpi.is.md.sim.PairGenerator;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

public class CrossPairGeneratorTest {

	@Test
	public void test() {
		PairGenerator<Object> generator = CrossPairGenerator.getInstance();
		Collection<Object> right = Arrays.asList(Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5));
		Collection<Object> left = Arrays.asList(Integer.valueOf(1), Integer.valueOf(2));
		Collection<Tuple2<Object, Collection<Object>>> similarities = generator
			.generate(left, right)
			.getPairs()
			.collect(Collectors.toList());
		assertThat(similarities).hasSize(2);
		assertThat(similarities).contains(new Tuple2<>(Integer.valueOf(1), Arrays.asList(Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5))));
		assertThat(similarities).contains(new Tuple2<>(Integer.valueOf(2), Arrays.asList(Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5))));
	}

}
