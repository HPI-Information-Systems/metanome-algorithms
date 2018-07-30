package de.hpi.is.md.hybrid.impl.sim.threshold;

import de.hpi.is.md.hybrid.SimilarityIndex;
import de.hpi.is.md.hybrid.impl.sim.SimilarityTableBuilder;
import de.hpi.is.md.util.Int2Int2DoubleTable;

class CollectingSimilarityReceiver extends ThresholdSimilarityReceiver {

	CollectingSimilarityReceiver(SimilarityTableBuilder similarityTableBuilder) {
		super(similarityTableBuilder);
	}

	@Override
	protected SimilarityIndex build(ThresholdMapBuilder thresholdMapBuilder,
		Int2Int2DoubleTable similarityTable, double minSimilarity) {
		ThresholdMap thresholdMap = thresholdMapBuilder.build();
		return ThresholdSimilarityIndex.builder()
			.thresholdMap(thresholdMap)
			.similarityTable(similarityTable)
			.minSimilarity(minSimilarity)
			.build();
	}

}
