package de.hpi.is.md.hybrid.impl.validation;

import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.util.IntArrayPair;
import it.unimi.dsi.fastutil.ints.IntIterable;
import java.util.ArrayList;
import java.util.Collection;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class TrivialRhsValidationTask implements RhsValidationTask {

	private final int rhsAttr;
	@NonNull
	private final Classifier classifier;
	private final double from;
	private final double lhsSimilarity;

	@Override
	public RhsResult createResult() {
		Collection<IntArrayPair> violations = new ArrayList<>();
		return RhsResult.builder()
			.rhsAttr(rhsAttr)
			.from(from)
			.threshold(lhsSimilarity)
			.violations(violations)
			.validAndMinimal(isValidAndMinimal())
			.build();
	}

	@Override
	public boolean shouldUpdate() {
		return false;
	}

	@Override
	public void validate(Iterable<int[]> leftMatches, IntIterable right) {
		// do nothing because we already know the result
	}

	@Override
	public void validate(int[] record, IntIterable right) {
		// do nothing because we already know the result
	}

	private boolean isValidAndMinimal(double similarity) {
		return classifier.isValidAndMinimal(similarity);
	}

	private boolean isValidAndMinimal() {
		return isValidAndMinimal(lhsSimilarity);
	}
}
