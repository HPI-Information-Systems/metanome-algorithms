package de.hpi.is.md.sim.impl;

import de.hpi.is.md.sim.Similarity;
import de.hpi.is.md.sim.Similarity.To;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class SimilarityCalculator<T> {

	private final SimilarityMeasure<T> similarityMeasure;

	Similarity<T> calculateSimilarities(T left, Iterable<T> right) {
		return with(left).calculateSimilarities(right);
	}

	private WithLeft with(T left) {
		return new WithLeft(left);
	}

	@RequiredArgsConstructor
	private class WithLeft {

		private final T left;

		private Similarity<T> calculateSimilarities(Iterable<T> right) {
			Collection<To<T>> similarities = StreamUtils.seq(right)
				.map(this::calculateSimilarity)
				.toList();
			return new Similarity<>(left, similarities);
		}

		private To<T> calculateSimilarity(T right) {
			double similarity = similarityMeasure.calculateSimilarity(left, right);
			return new To<>(right, similarity);
		}
	}

}
