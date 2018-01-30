package de.hpi.is.md.hybrid.impl.validation;

import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.Rhs;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import it.unimi.dsi.fastutil.ints.IntIterable;

public interface RhsValidationTask {

	RhsResult createResult();

	boolean shouldUpdate();

	void validate(Iterable<int[]> left, IntIterable right);

	void validate(int[] record, IntIterable right);

	interface Factory {

		RhsValidationTask create(Rhs rhs, PreprocessedColumnPair columnPair, double lhsSimilarity);
	}
}
