package de.hpi.is.md.hybrid.impl.validation.arbitrary;

import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.Rhs;
import de.hpi.is.md.hybrid.impl.ColumnPairWithThreshold;
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
public class ArbitraryValidationTaskFactory {

	@NonNull
	private final List<PreprocessedColumnPair> columnPairs;
	@NonNull
	private final LhsValidationTaskFactory factory;
	@NonNull
	private final RhsValidationTask.Factory rhsFactory;
	@NonNull
	private final RhsValidationTask.Factory trivialFactory;

	public static ArbitraryValidationTaskFactoryBuilder builder() {
		return new ArbitraryValidationTaskFactoryBuilder();
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

		private PreprocessedColumnPair asPair(MDElement mdElement) {
			int id = mdElement.getId();
			return getColumnPair(id);
		}

		private ColumnPairWithThreshold asPairWithThreshold(MDElement mdElement) {
			double threshold = mdElement.getThreshold();
			int id = mdElement.getId();
			PreprocessedColumnPair columnPair = getColumnPair(id);
			return new ColumnPairWithThreshold(columnPair, threshold);
		}

		private RhsValidationTask createRhsTask(Rhs rhs) {
			int rhsAttr = rhs.getRhsAttr();
			PreprocessedColumnPair columnPair = getColumnPair(rhsAttr);
			double lhsSimilarity = lhs.getOrDefault(rhsAttr);
			if (isTrivial(rhsAttr)) {
				return trivialFactory.create(rhs, columnPair, lhsSimilarity);
			}
			return rhsFactory.create(rhs, columnPair, lhsSimilarity);
		}

		private ValidationTask createTask(Iterable<Rhs> rhs) {
			Collection<ColumnPairWithThreshold> lhsPairs = getColumnPairsWithThreshold();
			Collection<RhsValidationTask> rhsTasks = StreamUtils.seq(rhs)
				.map(this::createRhsTask)
				.toList();
			List<PreprocessedColumnPair> columnPairs = getColumnPairs();
			return factory.create(lhsPairs, rhsTasks, lhs, columnPairs);
		}

		private PreprocessedColumnPair getColumnPair(int id) {
			return columnPairs.get(id);
		}

		private List<PreprocessedColumnPair> getColumnPairs() {
			return StreamUtils.seq(lhs)
				.map(this::asPair)
				.toList();
		}

		private Collection<ColumnPairWithThreshold> getColumnPairsWithThreshold() {
			return StreamUtils.seq(lhs)
				.map(this::asPairWithThreshold)
				.toList();
		}

		private boolean isTrivial(int rhsAttr) {
			return lhs.cardinality() == 1 && lhs.isSet(rhsAttr);
		}

	}

}
