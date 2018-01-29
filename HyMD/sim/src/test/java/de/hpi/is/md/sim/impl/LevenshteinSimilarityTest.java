package de.hpi.is.md.sim.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import de.hpi.is.md.sim.SimilarityMeasure;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LevenshteinSimilarityTest {

	private static final double ERROR = 1e-10;

	@Parameter
	public LevenshteinSimilarity similarityMeasure;

	@Parameters
	public static Collection<LevenshteinSimilarity> data() {
		return Arrays.asList(LevenshteinSimilarity.EXACT, LevenshteinSimilarity.FAST);
	}


	@Test
	public void test() {
		assertThat(similarityMeasure.calculateSimilarity("foo", "bar")).isEqualTo(0.0);
		assertThat(similarityMeasure.calculateSimilarity("bar", "baz"))
			.isCloseTo(2.0 / 3, within(ERROR));
		assertThat(similarityMeasure.calculateSimilarity("foo", "")).isCloseTo(0.0, within(ERROR));
	}

	@Test
	public void testBothEmpty() {
		assertThat(similarityMeasure.calculateSimilarity("", ""))
			.isEqualTo(SimilarityMeasure.MAX_SIMILARITY);
	}

	@Test
	public void testBothNull() {
		assertThat(similarityMeasure.calculateSimilarity(null, null))
			.isEqualTo(SimilarityMeasure.MAX_SIMILARITY);
	}

	@Test
	public void testOneNull() {
		assertThat(similarityMeasure.calculateSimilarity("foo", null))
			.isEqualTo(SimilarityMeasure.MIN_SIMILARITY);
		assertThat(similarityMeasure.calculateSimilarity(null, "foo"))
			.isEqualTo(SimilarityMeasure.MIN_SIMILARITY);
	}

}