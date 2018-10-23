package de.hpi.is.md.hybrid.impl.validation.single;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.hybrid.PositionListIndex.Cluster;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.ValidationResult;
import de.hpi.is.md.hybrid.ValidationResult.LhsResult;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.hybrid.impl.validation.RhsValidationTask;
import de.hpi.is.md.hybrid.impl.validation.ValidationTask;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.StreamUtils;
import it.unimi.dsi.fastutil.ints.IntCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.PrimitiveIterator.OfInt;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
class SingleValidationTask implements ValidationTask {

	@NonNull
	private final Collection<RhsValidationTask> rhs;
	@NonNull
	private final MDSite lhs;
	@NonNull
	private final PreprocessedColumnPair columnPair;
	private final double threshold;
	private final long minSupport;
	@NonNull
	private final DictionaryRecords leftRecords;
	private long support = 0;

	@Override
	public ValidationResult validate() {
		PositionListIndex pli = columnPair.getLeftPli();
		for (Cluster cluster : pli) {
			validate(cluster);
			if (canAbort()) {
				break;
			}
		}
		return collectResults();
	}

	private Collection<RhsResult> getResults() {
		return StreamUtils.seq(rhs)
			.map(RhsValidationTask::createResult)
			.toList();
	}

	private boolean allRejected() {
		return rhs.stream().noneMatch(RhsValidationTask::shouldUpdate);
	}

	private boolean canAbort() {
		return isSupported() && allRejected();
	}

	private ValidationResult collectResults() {
		LhsResult lhsResult = new LhsResult(lhs, support);
		Collection<RhsResult> rhsResults = getResults();
		return new ValidationResult(lhsResult, rhsResults);
	}

	private Iterable<int[]> getRecords(IntCollection leftMatches) {
		int size = leftMatches.size();
		Collection<int[]> left = new ArrayList<>(size);
		OfInt it = leftMatches.iterator();
		while (it.hasNext()) {
			int leftId = it.nextInt();
			int[] record = leftRecords.get(leftId);
			left.add(record);
		}
		return left;
	}

	private boolean isSupported() {
		return support >= minSupport;
	}

	private long validate(IntCollection leftMatches, IntCollection rightMatches) {
		log.trace("Validating {} left matches with {} right matches", Integer.valueOf(leftMatches.size()),
			Integer.valueOf(rightMatches.size()));
		Iterable<int[]> left = getRecords(leftMatches);
		rhs.forEach(rhsTask -> rhsTask.validate(left, rightMatches));
		return (long) leftMatches.size() * rightMatches.size();
	}

	private void validate(Cluster cluster) {
		int value = cluster.getValue();
		IntCollection leftMatches = cluster.getRecords();
		IntCollection rightMatches = columnPair.getAllSimilarRightRecords(value, threshold);
		support += validate(leftMatches, rightMatches);
	}
}
