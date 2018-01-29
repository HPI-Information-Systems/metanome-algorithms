package de.hpi.is.md.util.enforce;

import com.google.common.collect.Iterables;
import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.MatchingDependency.ColumnMatchWithThreshold;
import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.impl.ColumnPairWithThreshold;
import de.hpi.is.md.hybrid.impl.LhsSelector;
import de.hpi.is.md.hybrid.impl.RecordGrouper;
import de.hpi.is.md.hybrid.impl.RecordGrouperImpl;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;

@Builder
class EnforcerFactory {

	@NonNull
	private final DictionaryRecords leftRecords;
	@NonNull
	private final DictionaryRecords rightRecords;
	@NonNull
	private final Map<ColumnMapping<?>, PreprocessedColumnPair> columnPairs;

	ActualEnforcer createEnforcer(Collection<ColumnMatchWithThreshold<?>> lhs) {
		if (lhs.isEmpty()) {
			return createEmptyEnforcer();
		}
		if (lhs.size() == 1) {
			return createSingleEnforcer(lhs);
		}
		return createArbitraryEnforcer(lhs);
	}

	private PreprocessedColumnPair asPair(ColumnMatchWithThreshold<?> match) {
		ColumnMapping<?> columnPair = match.getMatch();
		return getColumnPair(columnPair);
	}

	private ColumnPairWithThreshold asPairWithThreshold(ColumnMatchWithThreshold<?> match) {
		double threshold = match.getThreshold();
		PreprocessedColumnPair preprocessedColumnPair = asPair(match);
		return new ColumnPairWithThreshold(preprocessedColumnPair, threshold);
	}

	private ActualEnforcer createArbitraryEnforcer(Iterable<ColumnMatchWithThreshold<?>> lhs) {
		Collection<ColumnPairWithThreshold> lhsPairs = getColumnPairsWithThreshold(lhs);
		LhsSelector intersector = new LhsSelector(lhsPairs, rightRecords);
		RecordGrouper grouper = createRecordGrouper(lhs);
		return ArbitraryActualEnforcer.builder()
			.grouper(grouper)
			.intersector(intersector)
			.rightRecords(new RecordSelector(rightRecords))
			.build();
	}

	private RecordGrouper createRecordGrouper(Iterable<ColumnMatchWithThreshold<?>> lhs) {
		List<PreprocessedColumnPair> columnPairs = getColumnPairs(lhs);
		return RecordGrouper.create(columnPairs, leftRecords);
	}

	private ActualEnforcer createEmptyEnforcer() {
		return new EmptyActualEnforcer(leftRecords, rightRecords);
	}

	private ActualEnforcer createSingleEnforcer(Iterable<ColumnMatchWithThreshold<?>> lhs) {
		ColumnMatchWithThreshold<?> columnMatch = Iterables.get(lhs, 0);
		PreprocessedColumnPair preprocessedColumnPair = asPair(columnMatch);
		double threshold = columnMatch.getThreshold();
		return SingleActualEnforcer.builder()
			.columnPair(preprocessedColumnPair)
			.threshold(threshold)
			.leftRecords(new RecordSelector(leftRecords))
			.rightRecords(new RecordSelector(rightRecords))
			.build();
	}

	private PreprocessedColumnPair getColumnPair(ColumnMapping<?> columnPair) {
		return Optional.ofNullable(columnPairs.get(columnPair))
			.orElseThrow(() -> new IllegalArgumentException("Unknown column pair: " + columnPair));
	}

	private List<PreprocessedColumnPair> getColumnPairs(Iterable<ColumnMatchWithThreshold<?>> lhs) {
		return StreamUtils.seq(lhs)
			.map(this::asPair)
			.toList();
	}

	private Collection<ColumnPairWithThreshold> getColumnPairsWithThreshold(
		Iterable<ColumnMatchWithThreshold<?>> lhs) {
		return StreamUtils.seq(lhs)
			.map(this::asPairWithThreshold)
			.toList();
	}

}
