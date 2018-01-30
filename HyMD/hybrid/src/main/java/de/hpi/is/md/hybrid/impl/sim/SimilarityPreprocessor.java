package de.hpi.is.md.hybrid.impl.sim;

import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.hybrid.impl.sim.PreprocessedSimilarity.To;
import de.hpi.is.md.sim.Similarity;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.Dictionary;
import de.hpi.is.md.util.StreamUtils;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collection;
import lombok.Builder;
import lombok.NonNull;

@Builder
class SimilarityPreprocessor<T> {

	@NonNull
	private final Dictionary<T> leftDictionary;
	@NonNull
	private final Dictionary<T> rightDictionary;
	private final double absoluteMinThreshold;
	@NonNull
	private final PositionListIndex rightIndex;

	PreprocessedSimilarity preprocess(Similarity<T> similarity) {
		Collection<Similarity.To<T>> similarities = similarity.getSimilarities();
		double minSimilarity = similarities.stream()
			.mapToDouble(Similarity.To::getSimilarity)
			.min()
			.orElse(SimilarityMeasure.MAX_SIMILARITY);
		Collection<PreprocessedSimilarity.To> preprocessedTos = preprocess(similarities);
		return asPreprocessed(similarity, preprocessedTos, minSimilarity);
	}

	private PreprocessedSimilarity asPreprocessed(Similarity<T> similarity,
		Collection<To> tos, double minSimilarity) {
		int leftValue = getLeftValue(similarity);
		return PreprocessedSimilarity.builder()
			.left(leftValue)
			.similarities(tos)
			.minSimilarity(minSimilarity)
			.build();
	}

	private int getLeftValue(Similarity<T> similarity) {
		T left = similarity.getLeft();
		return leftDictionary.getOrAdd(left);
	}

	private int getRightValue(Similarity.To<T> similarity) {
		T right = similarity.getRight();
		return rightDictionary.getOrAdd(right);
	}

	private boolean isRelevant(Similarity.To<T> similarity) {
		double sim = similarity.getSimilarity();
		return sim >= absoluteMinThreshold && sim > SimilarityMeasure.MIN_SIMILARITY;
	}

	private PreprocessedSimilarity.To preprocess(Similarity.To<T> similarity) {
		int right = getRightValue(similarity);
		double sim = similarity.getSimilarity();
		IntSet records = rightIndex.get(right);
		return PreprocessedSimilarity.To.builder()
			.right(right)
			.similarity(sim)
			.records(records)
			.build();
	}

	private Collection<PreprocessedSimilarity.To> preprocess(
		Iterable<Similarity.To<T>> similarities) {
		return StreamUtils.seq(similarities)
			.filter(this::isRelevant)
			.map(this::preprocess)
			.toList();
	}
}
