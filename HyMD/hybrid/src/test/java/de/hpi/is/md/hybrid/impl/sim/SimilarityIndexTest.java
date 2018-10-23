package de.hpi.is.md.hybrid.impl.sim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.hybrid.SimilarityIndex;
import de.hpi.is.md.hybrid.SimilarityIndex.SimilarityIndexBuilder;
import de.hpi.is.md.sim.Similarity;
import de.hpi.is.md.sim.Similarity.To;
import de.hpi.is.md.sim.SimilarityComputer;
import de.hpi.is.md.sim.SimilarityComputer.Result;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.Dictionary;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

public abstract class SimilarityIndexTest {

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);
	@Mock
	private Dictionary<Integer> leftDictionary;
	@Mock
	private Dictionary<Integer> rightDictionary;
	@Mock
	private PositionListIndex rightIndex;

	@Test
	public void testGetSimilarRecords() {
		SimilarityIndex index = createSimilarityIndex();
		assertThat(index.getSimilarRecords(1, 1.0 / 4)).hasSize(4);
		assertThat(index.getSimilarRecords(1, 1.0 / 4)).contains(Integer.valueOf(13), Integer.valueOf(14), Integer.valueOf(15), Integer.valueOf(17));
		assertThat(index.getSimilarRecords(1, 1.0 / 3)).hasSize(2);
		assertThat(index.getSimilarRecords(1, 1.0 / 3)).contains(Integer.valueOf(13), Integer.valueOf(17));
	}

	@Test
	public void testGetSimilarities() {
		SimilarityIndex index = createSimilarityIndex();
		assertThat(index.getSimilarities()).hasSize(3);
		assertThat(index.getSimilarities()).contains(Double.valueOf(1.0 / 3), Double.valueOf(1.0 / 4), Double.valueOf(1.0 / 6));
	}

	@Test
	public void testGetSimilarity() {
		SimilarityIndex index = createSimilarityIndex();
		assertThat(index.getSimilarity(1, 3)).isEqualTo(1.0 / 3);
		assertThat(index.getSimilarity(1, 2)).isEqualTo(SimilarityMeasure.MIN_SIMILARITY);
	}

	protected abstract SimilarityIndexBuilder createBuilder();

	@SuppressWarnings("unchecked")
	private SimilarityIndex createSimilarityIndex() {
		SimilarityComputer<Integer> similarityComputer = Mockito.mock(SimilarityComputer.class);
		SimilarityMeasure<Integer> similarityMeasure = Mockito.mock(SimilarityMeasure.class);
		Collection<Integer> leftValues = Arrays.asList(Integer.valueOf(1), Integer.valueOf(2));
		Collection<Integer> rightValues = Arrays.asList(Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6));
		when(similarityComputer.compute(similarityMeasure, leftValues, rightValues))
			.thenReturn(new Result<>(Stream.of(new Similarity<>(Integer.valueOf(1), Arrays.asList(
				new To<>(Integer.valueOf(3), 1.0 / 3),
				new To<>(Integer.valueOf(4), 1.0 / 4),
				new To<>(Integer.valueOf(5), 1.0 / 4),
				new To<>(Integer.valueOf(6), 1.0 / 6)
			))), true));
		SimilarityIndexBuilder builder = createBuilder();
		when(leftDictionary.values()).thenReturn(leftValues);
		when(Integer.valueOf(leftDictionary.size())).thenReturn(Integer.valueOf(3));
		when(rightDictionary.values()).thenReturn(rightValues);
		when(Integer.valueOf(rightDictionary.size())).thenReturn(Integer.valueOf(7));
		when(Integer.valueOf(leftDictionary.getOrAdd(Integer.valueOf(1)))).thenReturn(Integer.valueOf(1));
		doReturn(new IntOpenHashSet(Arrays.asList(Integer.valueOf(13), Integer.valueOf(17)))).when(rightIndex).get(3);
		doReturn(IntSets.singleton(14)).when(rightIndex).get(4);
		doReturn(IntSets.singleton(15)).when(rightIndex).get(5);
		doReturn(IntSets.singleton(16)).when(rightIndex).get(6);
		doReturn(Integer.valueOf(3)).when(rightDictionary).getOrAdd(Integer.valueOf(3));
		doReturn(Integer.valueOf(4)).when(rightDictionary).getOrAdd(Integer.valueOf(4));
		doReturn(Integer.valueOf(5)).when(rightDictionary).getOrAdd(Integer.valueOf(5));
		doReturn(Integer.valueOf(6)).when(rightDictionary).getOrAdd(Integer.valueOf(6));
		return builder
			.create(leftDictionary, rightDictionary, similarityMeasure, similarityComputer, 0.0,
				rightIndex);
	}
}
