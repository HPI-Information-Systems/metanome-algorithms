package de.hpi.is.md.hybrid.impl.sim;

import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.hybrid.SimilarityIndex;
import de.hpi.is.md.hybrid.SimilarityIndex.SimilarityIndexBuilder;
import de.hpi.is.md.sim.SimilarityComputer;
import de.hpi.is.md.sim.SimilarityComputer.Result;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.Dictionary;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;

public abstract class AbstractSimilarityIndexBuilder implements SimilarityIndexBuilder {

	@Override
	public <T> SimilarityIndex create(Dictionary<T> leftDictionary, Dictionary<T> rightDictionary,
		SimilarityMeasure<T> similarityMeasure, SimilarityComputer<T> computer, double minThreshold,
		PositionListIndex rightIndex) {
		SimilarityPreprocessor<T> preprocessor = SimilarityPreprocessor.<T>builder()
			.leftDictionary(leftDictionary)
			.rightDictionary(rightDictionary)
			.absoluteMinThreshold(minThreshold)
			.rightIndex(rightIndex)
			.build();
		BuildTask<T> task = createTask(computer, similarityMeasure, preprocessor,
			leftDictionary.size(),
			rightDictionary.size());
		Collection<T> left = leftDictionary.values();
		Collection<T> right = rightDictionary.values();
		return task.build(left, right);
	}

	protected abstract SimilarityReceiver createReceiver(int leftSize, int rightSize);

	private <T> BuildTask<T> createTask(SimilarityComputer<T> computer,
		SimilarityMeasure<T> similarityMeasure, SimilarityPreprocessor<T> preprocessor,
		int leftSize, int rightSize) {
		SimilarityReceiver receiver = createReceiver(leftSize, rightSize);
		return BuildTask.<T>builder()
			.similarityMeasure(similarityMeasure)
			.receiver(receiver)
			.computer(computer)
			.preprocessor(preprocessor)
			.build();
	}

	@Builder
	private static class BuildTask<T> {

		@NonNull
		private final SimilarityMeasure<T> similarityMeasure;
		@NonNull
		private final SimilarityComputer<T> computer;
		@NonNull
		private final SimilarityPreprocessor<T> preprocessor;
		@NonNull
		private final SimilarityReceiver receiver;

		private SimilarityIndex build(Collection<T> left, Collection<T> right) {
			//collect to benefit from parallelization
			//preprocessed similarities need to be consumed sequentially
			Result<T> result = computer.compute(similarityMeasure, left, right);
			Collection<PreprocessedSimilarity> similarities = result.getSimilarities()
				.map(preprocessor::preprocess)
				.collect(Collectors.toList());
			double minSimilarity = result.isComplete() ? similarities.stream()
				.mapToDouble(PreprocessedSimilarity::getMinSimilarity)
				.min()
				.orElse(SimilarityMeasure.MAX_SIMILARITY) : SimilarityMeasure.MIN_SIMILARITY;
			Iterable<PreprocessedSimilarity> filtered = StreamUtils.seq(similarities)
				.filter(PreprocessedSimilarity::isNotEmpty)
				.toList();
			return buildIndex(filtered, minSimilarity);
		}

		private SimilarityIndex buildIndex(Iterable<PreprocessedSimilarity> similarities,
			double minSimilarity) {
			similarities.forEach(receiver::addSimilarity);
			return receiver.build(minSimilarity);
		}

	}

}
