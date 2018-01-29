package de.hpi.is.md.hybrid.impl.validation;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.Rhs;
import de.hpi.is.md.hybrid.impl.validation.RhsValidationTask.Factory;
import de.hpi.is.md.util.IntArrayPair;
import java.util.Collection;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.NonNull;

@Builder
class RhsValidationTaskImplFactory implements Factory {

	@NonNull
	private final DictionaryRecords rightRecords;
	private final double minThreshold;
	@NonNull
	private final Predicate<Collection<IntArrayPair>> shouldUpdate;

	@Override
	public RhsValidationTask create(Rhs rhs, PreprocessedColumnPair columnPair,
		double lhsSimilarity) {
		double from = rhs.getThreshold();
		int rhsAttr = rhs.getRhsAttr();
		Classifier classifier = createClassifier(rhs);
		return RhsValidationTaskImpl.builder()
			.columnPair(columnPair)
			.classifier(classifier)
			.rightRecords(rightRecords)
			.rhsAttr(rhsAttr)
			.from(from)
			.shouldUpdate(shouldUpdate)
			.lhsSimilarity(lhsSimilarity)
			.build();
	}

	private Classifier createClassifier(Rhs rhs) {
		double lowerBound = rhs.getLowerBound();
		return new Classifier(minThreshold, lowerBound);
	}

}
