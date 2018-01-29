package de.hpi.is.md.sim.impl;

import static org.assertj.core.api.Assertions.assertThat;

import de.hpi.is.md.sim.SimilarityMeasure;
import org.junit.Test;

public class EqualsSimilarityMeasureTest {

	@Test
	public void test() {
		String obj1 = "foo";
		String obj2 = "bar";
		assertThat(obj1).isNotEqualTo(obj2);
		SimilarityMeasure<Object> sim = EqualsSimilarityMeasure.INSTANCE;
		assertThat(sim.calculateSimilarity(obj1, obj1)).isEqualTo(SimilarityMeasure.MAX_SIMILARITY);
		assertThat(sim.calculateSimilarity(obj1, obj2)).isEqualTo(SimilarityMeasure.MIN_SIMILARITY);
		assertThat(sim.calculateSimilarity(obj2, obj1)).isEqualTo(SimilarityMeasure.MIN_SIMILARITY);
	}

	@Test
	public void testNullSafety() {
		String obj1 = "foo";
		SimilarityMeasure<Object> sim = EqualsSimilarityMeasure.INSTANCE;
		assertThat(sim.calculateSimilarity(obj1, null)).isEqualTo(SimilarityMeasure.MIN_SIMILARITY);
		assertThat(sim.calculateSimilarity(null, obj1)).isEqualTo(SimilarityMeasure.MIN_SIMILARITY);
		assertThat(sim.calculateSimilarity(null, null)).isEqualTo(SimilarityMeasure.MAX_SIMILARITY);
	}

}
