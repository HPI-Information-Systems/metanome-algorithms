package de.hpi.is.md.hybrid.impl.sim.threshold;

import de.hpi.is.md.hybrid.SimilarityIndex;
import de.hpi.is.md.util.Int2Int2DoubleTable;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
class ThresholdSimilarityIndex implements SimilarityIndex {

	private static final long serialVersionUID = -3954177832644508372L;
	@NonNull
	private final ThresholdMap thresholdMap;
	@NonNull
	private final Int2Int2DoubleTable similarityTable;
	@Getter
	private final double minSimilarity;

	@Override
	public IntCollection getSimilarRecords(int valueId, double threshold) {
		return thresholdMap.greaterOrEqual(valueId, threshold);
	}

	@Override
	public DoubleSet getSimilarities() {
		return similarityTable.values();
	}

	@Override
	public double getSimilarity(int leftValue, int rightValue) {
		return similarityTable.getOrDefault(leftValue, rightValue);
	}
}
