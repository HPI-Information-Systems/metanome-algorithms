package de.hpi.is.md.hybrid.impl;

import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ColumnPairWithThreshold {

	@NonNull
	private final PreprocessedColumnPair columnPair;
	private final double threshold;

	IntCollection getMatching(int value) {
		return columnPair.getAllSimilarRightRecords(value, threshold);
	}
}
