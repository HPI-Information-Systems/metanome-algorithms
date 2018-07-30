package de.hpi.is.md.hybrid.impl.validation;

import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.Rhs;
import de.hpi.is.md.hybrid.impl.validation.RhsValidationTask.Factory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TrivialRhsValidationTaskFactory implements Factory {

	private final double minThreshold;

	@Override
	public RhsValidationTask create(Rhs rhs, PreprocessedColumnPair columnPair,
		double lhsSimilarity) {
		double from = rhs.getThreshold();
		int rhsAttr = rhs.getRhsAttr();
		Classifier classifier = createClassifier(rhs);
		return TrivialRhsValidationTask.builder()
			.classifier(classifier)
			.from(from)
			.lhsSimilarity(lhsSimilarity)
			.rhsAttr(rhsAttr)
			.build();
	}

	private Classifier createClassifier(Rhs rhs) {
		double lowerBound = rhs.getLowerBound();
		return new Classifier(minThreshold, lowerBound);
	}

}
