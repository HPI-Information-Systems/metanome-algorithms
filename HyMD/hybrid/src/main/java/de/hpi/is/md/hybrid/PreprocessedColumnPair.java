package de.hpi.is.md.hybrid;

import de.hpi.is.md.ThresholdFilter;
import it.unimi.dsi.fastutil.ints.IntCollection;
import java.io.Serializable;

public interface PreprocessedColumnPair extends Serializable {

	IntCollection getAllSimilarRightRecords(int[] left, double threshold);

	IntCollection getAllSimilarRightRecords(int leftValue, double threshold);

	PositionListIndex getLeftPli();

	int getLeftValue(int[] left);

	double getMinSimilarity();

	int getRightValue(int[] right);

	default double getSimilarity(int[] left, int[] right) {
		int leftValue = getLeftValue(left);
		int rightValue = getRightValue(right);
		return getSimilarity(leftValue, rightValue);
	}

	double getSimilarity(int leftValue, int rightValue);

	Iterable<Double> getThresholds(ThresholdFilter thresholdFilter);
}
