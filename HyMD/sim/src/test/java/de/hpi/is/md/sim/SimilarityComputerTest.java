package de.hpi.is.md.sim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import de.hpi.is.md.sim.PairGenerator.Result;
import de.hpi.is.md.sim.Similarity.To;
import de.hpi.is.md.sim.impl.SimilarityComputerImpl;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.lambda.tuple.Tuple;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

public abstract class SimilarityComputerTest {

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);
	@Mock
	private PairGenerator<Integer> generator;
	@Mock
	private SimilarityMeasure<Integer> similarityMeasure;

	@Test
	public void test() {
		doReturn(0.13).when(similarityMeasure).calculateSimilarity(1, 3);
		doReturn(0.14).when(similarityMeasure).calculateSimilarity(1, 4);
		doReturn(0.23).when(similarityMeasure).calculateSimilarity(2, 3);
		doReturn(0.24).when(similarityMeasure).calculateSimilarity(2, 4);
		when(generator.generate(Arrays.asList(1, 2), Arrays.asList(3, 4)))
			.thenReturn(new Result<>(Stream.of(
				Tuple.tuple(1, Arrays.asList(3, 4)),
				Tuple.tuple(2, Arrays.asList(3, 4))), true));
		SimilarityComputer<Integer> computer = createComputer();
		Collection<Similarity<Integer>> similarities = computer
			.compute(similarityMeasure, Arrays.asList(1, 2), Arrays.asList(3, 4))
			.getSimilarities()
			.collect(Collectors.toList());
		assertThat(similarities).hasSize(2);
		assertThat(similarities)
			.contains(new Similarity<>(1, Arrays.asList(new To<>(3, 0.13), new To<>(4, 0.14))));
		assertThat(similarities)
			.contains(new Similarity<>(2, Arrays.asList(new To<>(3, 0.23), new To<>(4, 0.24))));
	}

	protected abstract <T> SimilarityComputerImpl<T> createComputer(PairGenerator<T> generator);

	private SimilarityComputer<Integer> createComputer() {
		return createComputer(generator);
	}
}
