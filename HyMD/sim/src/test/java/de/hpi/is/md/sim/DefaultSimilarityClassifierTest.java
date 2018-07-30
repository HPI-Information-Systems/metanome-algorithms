package de.hpi.is.md.sim;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DefaultSimilarityClassifierTest {

	private static SimilarityClassifier<String> getClassifier(double threshold) {
		SimilarityMeasure<String> similarityMeasure = (obj1, obj2) -> 0.5;
		return similarityMeasure.asClassifier(threshold);
	}

	@Test
	public void test() {
		assertThat(getClassifier(0.6).areSimilar("foo", "bar")).isFalse();
		assertThat(getClassifier(0.5).areSimilar("foo", "bar")).isTrue();
		assertThat(getClassifier(0.4).areSimilar("foo", "bar")).isTrue();
	}

	@Test
	public void testNestedClassifier() {
		assertThat(getClassifier(0.6).asClassifier(0.4).areSimilar("foo", "bar")).isTrue();
		assertThat(getClassifier(0.4).asClassifier(0.6).areSimilar("foo", "bar")).isFalse();
	}

}
