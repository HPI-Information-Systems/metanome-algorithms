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
		doReturn(Double.valueOf(0.13)).when(similarityMeasure).calculateSimilarity(Integer.valueOf(1), Integer.valueOf(3));
		doReturn(Double.valueOf(0.14)).when(similarityMeasure).calculateSimilarity(Integer.valueOf(1), Integer.valueOf(4));
		doReturn(Double.valueOf(0.23)).when(similarityMeasure).calculateSimilarity(Integer.valueOf(2), Integer.valueOf(3));
		doReturn(Double.valueOf(0.24)).when(similarityMeasure).calculateSimilarity(Integer.valueOf(2), Integer.valueOf(4));
		when(generator.generate(Arrays.asList(Integer.valueOf(1), Integer.valueOf(2)), Arrays.asList(Integer.valueOf(3), Integer.valueOf(4))))
			.thenReturn(new Result<>(Stream.of(
				Tuple.tuple(Integer.valueOf(1), Arrays.asList(Integer.valueOf(3), Integer.valueOf(4))),
				Tuple.tuple(Integer.valueOf(2), Arrays.asList(Integer.valueOf(3), Integer.valueOf(4)))), true));
		SimilarityComputer<Integer> computer = createComputer();
		Collection<Similarity<Integer>> similarities = computer
			.compute(similarityMeasure, Arrays.asList(Integer.valueOf(1), Integer.valueOf(2)), Arrays.asList(Integer.valueOf(3), Integer.valueOf(4)))
			.getSimilarities()
			.collect(Collectors.toList());
		assertThat(similarities).hasSize(2);
		assertThat(similarities)
			.contains(new Similarity<>(Integer.valueOf(1), Arrays.asList(new To<>(Integer.valueOf(3), 0.13), new To<>(Integer.valueOf(4), 0.14))));
		assertThat(similarities)
			.contains(new Similarity<>(Integer.valueOf(2), Arrays.asList(new To<>(Integer.valueOf(3), 0.23), new To<>(Integer.valueOf(4), 0.24))));
	}

	protected abstract <T> SimilarityComputerImpl<T> createComputer(PairGenerator<T> generator);

	private SimilarityComputer<Integer> createComputer() {
		return createComputer(generator);
	}
}
