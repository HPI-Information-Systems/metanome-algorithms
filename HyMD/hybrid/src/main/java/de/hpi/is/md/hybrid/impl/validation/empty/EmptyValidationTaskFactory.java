package de.hpi.is.md.hybrid.impl.validation.empty;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.Rhs;
import de.hpi.is.md.hybrid.impl.validation.Classifier;
import de.hpi.is.md.hybrid.impl.validation.ValidationTask;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class EmptyValidationTaskFactory {

	@NonNull
	private final DictionaryRecords leftRecords;
	@NonNull
	private final DictionaryRecords rightRecords;
	@NonNull
	private final List<PreprocessedColumnPair> columnPairs;
	private final double minThreshold;

	public ValidationTask createTask(MDSite lhs, Iterable<Rhs> rhs) {
		Collection<RhsValidationTask> rhsTasks = StreamUtils.seq(rhs)
			.map(this::createRhsTask)
			.toList();
		return EmptyValidationTask.builder()
			.leftRecords(leftRecords)
			.rightRecords(rightRecords)
			.lhs(lhs)
			.rhs(rhsTasks)
			.build();
	}

	private Classifier createClassifier(Rhs rhs) {
		double lowerBound = rhs.getLowerBound();
		return new Classifier(minThreshold, lowerBound);
	}

	private RhsValidationTask createRhsTask(Rhs rhs) {
		Classifier classifier = createClassifier(rhs);
		int rhsAttr = rhs.getRhsAttr();
		double from = rhs.getThreshold();
		PreprocessedColumnPair columnPair = columnPairs.get(rhsAttr);
		return RhsValidationTask.builder()
			.classifier(classifier)
			.columnPair(columnPair)
			.rhsAttr(rhsAttr)
			.from(from)
			.build();
	}
}
