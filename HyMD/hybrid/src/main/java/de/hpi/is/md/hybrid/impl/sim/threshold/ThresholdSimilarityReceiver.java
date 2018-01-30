package de.hpi.is.md.hybrid.impl.sim.threshold;

import de.hpi.is.md.hybrid.SimilarityIndex;
import de.hpi.is.md.hybrid.impl.sim.PreprocessedSimilarity;
import de.hpi.is.md.hybrid.impl.sim.PreprocessedSimilarity.To;
import de.hpi.is.md.hybrid.impl.sim.SimilarityReceiver;
import de.hpi.is.md.hybrid.impl.sim.SimilarityTableBuilder;
import de.hpi.is.md.util.Int2Int2DoubleTable;
import java.util.Collection;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class ThresholdSimilarityReceiver implements SimilarityReceiver {

	private final ThresholdMapBuilder thresholdMapBuilder = CollectingThresholdMap.builder();
	private final SimilarityTableBuilder similarityTableBuilder;

	@Override
	public void addSimilarity(PreprocessedSimilarity similarity) {
		int left = similarity.getLeft();
		Collection<To> similarities = similarity.getSimilarities();
		similarityTableBuilder.add(left, similarities);
		thresholdMapBuilder.add(left, similarities);
	}

	@Override
	public SimilarityIndex build(double minSimilarity) {
		Int2Int2DoubleTable similarityTable = similarityTableBuilder.build();
		return build(thresholdMapBuilder, similarityTable, minSimilarity);
	}

	protected abstract SimilarityIndex build(ThresholdMapBuilder thresholdMap,
		Int2Int2DoubleTable similarityTable, double minSimilarity);
}
