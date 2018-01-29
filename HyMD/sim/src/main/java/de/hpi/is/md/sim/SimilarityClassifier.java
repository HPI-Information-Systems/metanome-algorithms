package de.hpi.is.md.sim;

public interface SimilarityClassifier<T> {

	boolean areSimilar(T obj1, T obj2);

	SimilarityClassifier<T> asClassifier(double threshold);

	double getThreshold();
}
