package de.hpi.is.md.hybrid.impl.validation.single;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.Rhs;
import de.hpi.is.md.hybrid.impl.validation.RhsValidationTask;
import de.hpi.is.md.hybrid.impl.validation.ValidationTask;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SingleValidationTaskFactory {

	@NonNull
	private final DictionaryRecords leftRecords;
	@NonNull
	private final List<PreprocessedColumnPair> columnPairs;
	private final long minSupport;
	@NonNull
	private final RhsValidationTask.Factory rhsFactory;
	@NonNull
	private final RhsValidationTask.Factory trivialFactory;

	public static SingleValidationTaskFactoryBuilder builder() {
		return new SingleValidationTaskFactoryBuilder();
	}

	public ValidationTask createTask(MDSite lhs, Iterable<Rhs> rhs) {
		return with(lhs).createTask(rhs);
	}

	private WithLhs with(MDSite lhs) {
		return new WithLhs(lhs);
	}

	@RequiredArgsConstructor
	private class WithLhs {

		private final MDSite lhs;

		private RhsValidationTask createRhsTask(Rhs rhs) {
			int rhsAttr = rhs.getRhsAttr();
			PreprocessedColumnPair columnPair = columnPairs.get(rhsAttr);
			double lhsSimilarity = lhs.getOrDefault(rhsAttr);
			if (isTrivial(rhsAttr)) {
				return trivialFactory.create(rhs, columnPair, lhsSimilarity);

			}
			return rhsFactory.create(rhs, columnPair, lhsSimilarity);
		}

		private ValidationTask createTask(Collection<RhsValidationTask> rhsTasks) {
			MDElement elem = lhs.nextElement(0)
				.orElseThrow(IllegalArgumentException::new);
			int lhsAttr = elem.getId();
			PreprocessedColumnPair columnPair = columnPairs.get(lhsAttr);
			double threshold = elem.getThreshold();
			return SingleValidationTask.builder()
				.columnPair(columnPair)
				.minSupport(minSupport)
				.threshold(threshold)
				.lhs(lhs)
				.rhs(rhsTasks)
				.leftRecords(leftRecords)
				.build();
		}

		private ValidationTask createTask(Iterable<Rhs> rhs) {
			Collection<RhsValidationTask> rhsTasks = StreamUtils.seq(rhs)
				.map(this::createRhsTask)
				.toList();
			return createTask(rhsTasks);
		}

		private boolean isTrivial(int rhsAttr) {
			return lhs.cardinality() == 1 && lhs.isSet(rhsAttr);
		}
	}
}
