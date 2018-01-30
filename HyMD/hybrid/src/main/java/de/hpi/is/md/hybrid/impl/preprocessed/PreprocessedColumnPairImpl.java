package de.hpi.is.md.hybrid.impl.preprocessed;

import com.codahale.metrics.annotation.Timed;
import de.hpi.is.md.ThresholdFilter;
import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.SimilarityIndex;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PreprocessedColumnPairImpl implements PreprocessedColumnPair {

	private static final long serialVersionUID = -4122856934192664031L;
	private final int leftColumn;
	private final int rightColumn;
	@NonNull
	private final SimilarityIndex similarityIndex;
	@NonNull
	@Getter
	private final PositionListIndex leftPli;

	static PreprocessedColumnPairBuilder builder() {
		return new PreprocessedColumnPairBuilder();
	}

	@Timed
	@Override
	public IntCollection getAllSimilarRightRecords(int[] left, double threshold) {
		int leftValue = left[leftColumn];
		return getAllSimilarRightRecords(leftValue, threshold);
	}

	@Override
	public IntCollection getAllSimilarRightRecords(int leftValue, double threshold) {
		return similarityIndex.getSimilarRecords(leftValue, threshold);
	}

	@Override
	public double getMinSimilarity() {
		return similarityIndex.getMinSimilarity();
	}

	@Override
	public Iterable<Double> getThresholds(ThresholdFilter thresholdFilter) {
		DoubleSet similarities = similarityIndex.getSimilarities();
		return thresholdFilter.filter(similarities);
	}

	@Override
	public double getSimilarity(int leftValue, int rightValue) {
		return similarityIndex.getSimilarity(leftValue, rightValue);
	}

	@Override
	public int getRightValue(int[] right) {
		return right[rightColumn];
	}

	@Override
	public int getLeftValue(int[] left) {
		return left[leftColumn];
	}

}
