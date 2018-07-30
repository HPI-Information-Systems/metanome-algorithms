package de.hpi.is.md.hybrid.impl.validation.empty;

import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.hybrid.impl.validation.Classifier;
import java.util.Collections;
import lombok.Builder;
import lombok.NonNull;

@Builder
class RhsValidationTask {

	@NonNull
	private final PreprocessedColumnPair columnPair;
	@NonNull
	private final Classifier classifier;
	private final int rhsAttr;
	private final double from;

	RhsResult createResult() {
		double threshold = columnPair.getMinSimilarity();
		boolean validAndMinimal = classifier.isValidAndMinimal(threshold);
		return RhsResult.builder()
			.rhsAttr(rhsAttr)
			.from(from)
			.threshold(threshold)
			.validAndMinimal(validAndMinimal)
			.violations(Collections.emptyList())
			.build();
	}
}
