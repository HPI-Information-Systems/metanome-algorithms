package de.hpi.is.md.sim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.hpi.is.md.util.UnorderedPair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class CachedSimilarityMeasureTest {

	@Mock
	private SimilarityMeasure<Integer> sim;

	@Test
	public void test() {
		doReturn(Double.valueOf(0.5)).when(sim).calculateSimilarity(UnorderedPair.of(Integer.valueOf(1), Integer.valueOf(2)));
		doReturn(Double.valueOf(1.0 / 3)).when(sim).calculateSimilarity(UnorderedPair.of(Integer.valueOf(1), Integer.valueOf(3)));
		SimilarityMeasure<Integer> cachedSim = CachedSimilarityMeasure.<Integer>builder()
			.similarityMeasure(sim)
			.build();
		assertThat(cachedSim.calculateSimilarity(Integer.valueOf(1), Integer.valueOf(2))).isEqualTo(0.5);
		assertThat(cachedSim.calculateSimilarity(Integer.valueOf(2), Integer.valueOf(1))).isEqualTo(0.5);
		assertThat(cachedSim.calculateSimilarity(Integer.valueOf(1), Integer.valueOf(3))).isEqualTo(1.0 / 3);
	}

	@Test
	public void testCache() {
		SimilarityMeasure<Integer> cachedSim = CachedSimilarityMeasure.<Integer>builder()
			.similarityMeasure(sim)
			.maximumSize(1)
			.build();
		cachedSim.calculateSimilarity(Integer.valueOf(1), Integer.valueOf(2));
		verify(sim).calculateSimilarity(UnorderedPair.of(Integer.valueOf(1), Integer.valueOf(2)));
		cachedSim.calculateSimilarity(Integer.valueOf(1), Integer.valueOf(2));
		verify(sim).calculateSimilarity(UnorderedPair.of(Integer.valueOf(1), Integer.valueOf(2)));
	}

	@Test
	public void testNoCache() {
		SimilarityMeasure<Integer> cachedSim = CachedSimilarityMeasure.<Integer>builder()
			.similarityMeasure(sim)
			.maximumSize(0)
			.build();
		cachedSim.calculateSimilarity(Integer.valueOf(1), Integer.valueOf(2));
		verify(sim).calculateSimilarity(UnorderedPair.of(Integer.valueOf(1), Integer.valueOf(2)));
		cachedSim.calculateSimilarity(Integer.valueOf(1), Integer.valueOf(2));
		verify(sim, times(2)).calculateSimilarity(UnorderedPair.of(Integer.valueOf(1), Integer.valueOf(2)));
	}

}
