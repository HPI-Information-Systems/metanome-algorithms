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
		doReturn(0.5).when(sim).calculateSimilarity(UnorderedPair.of(1, 2));
		doReturn(1.0 / 3).when(sim).calculateSimilarity(UnorderedPair.of(1, 3));
		SimilarityMeasure<Integer> cachedSim = CachedSimilarityMeasure.<Integer>builder()
			.similarityMeasure(sim)
			.build();
		assertThat(cachedSim.calculateSimilarity(1, 2)).isEqualTo(0.5);
		assertThat(cachedSim.calculateSimilarity(2, 1)).isEqualTo(0.5);
		assertThat(cachedSim.calculateSimilarity(1, 3)).isEqualTo(1.0 / 3);
	}

	@Test
	public void testCache() {
		SimilarityMeasure<Integer> cachedSim = CachedSimilarityMeasure.<Integer>builder()
			.similarityMeasure(sim)
			.maximumSize(1)
			.build();
		cachedSim.calculateSimilarity(1, 2);
		verify(sim).calculateSimilarity(UnorderedPair.of(1, 2));
		cachedSim.calculateSimilarity(1, 2);
		verify(sim).calculateSimilarity(UnorderedPair.of(1, 2));
	}

	@Test
	public void testNoCache() {
		SimilarityMeasure<Integer> cachedSim = CachedSimilarityMeasure.<Integer>builder()
			.similarityMeasure(sim)
			.maximumSize(0)
			.build();
		cachedSim.calculateSimilarity(1, 2);
		verify(sim).calculateSimilarity(UnorderedPair.of(1, 2));
		cachedSim.calculateSimilarity(1, 2);
		verify(sim, times(2)).calculateSimilarity(UnorderedPair.of(1, 2));
	}

}
