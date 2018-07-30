package de.hpi.is.md.sim;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultSimilarityClassifier<T> implements SimilarityClassifier<T> {

	@Getter
	private final double threshold;
	@NonNull
	private final SimilarityMeasure<T> similarityMeasure;

	@Override
	public boolean areSimilar(T obj1, T obj2) {
		return calculateSimilarity(obj1, obj2) >= threshold;
	}

	@Override
	public SimilarityClassifier<T> asClassifier(double newThreshold) {
		return new DefaultSimilarityClassifier<>(newThreshold, similarityMeasure);
	}

	@Override
	public String toString() {
		return similarityMeasure + "@" + threshold;
	}

	private double calculateSimilarity(T obj1, T obj2) {
		return similarityMeasure.calculateSimilarity(obj1, obj2);
	}
}
