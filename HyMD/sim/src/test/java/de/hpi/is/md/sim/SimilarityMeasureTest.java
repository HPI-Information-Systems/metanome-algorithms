package de.hpi.is.md.sim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;

import de.hpi.is.md.util.UnorderedPair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SimilarityMeasureTest {

	@Mock
	private SimilarityMeasure<Integer> similarityMeasure;

	@Test
	public void testWithPair() {
		doReturn(Double.valueOf(0.5)).when(similarityMeasure).calculateSimilarity(Integer.valueOf(1), Integer.valueOf(2));
		doCallRealMethod().when(similarityMeasure).calculateSimilarity(UnorderedPair.of(Integer.valueOf(1), Integer.valueOf(2)));
		assertThat(similarityMeasure.calculateSimilarity(UnorderedPair.of(Integer.valueOf(1), Integer.valueOf(2)))).isEqualTo(0.5);
	}

	@Test
	public void testWithPairOfSame() {
		doReturn(Double.valueOf(0.5)).when(similarityMeasure).calculateSimilarity(Integer.valueOf(1), Integer.valueOf(1));
		doCallRealMethod().when(similarityMeasure).calculateSimilarity(UnorderedPair.of(Integer.valueOf(1), Integer.valueOf(1)));
		assertThat(similarityMeasure.calculateSimilarity(UnorderedPair.of(Integer.valueOf(1), Integer.valueOf(1)))).isEqualTo(0.5);
	}

}