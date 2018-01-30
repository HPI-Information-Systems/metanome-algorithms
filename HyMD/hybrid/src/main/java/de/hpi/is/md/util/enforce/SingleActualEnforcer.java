package de.hpi.is.md.util.enforce;

import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.hybrid.PositionListIndex.Cluster;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import it.unimi.dsi.fastutil.ints.IntCollection;
import java.util.ArrayList;
import java.util.Collection;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
class SingleActualEnforcer implements ActualEnforcer {

	@NonNull
	private final PreprocessedColumnPair columnPair;
	private final double threshold;
	@NonNull
	private final RecordSelector leftRecords;
	@NonNull
	private final RecordSelector rightRecords;

	@Override
	public Collection<CompressedEnforceMatch> enforce() {
		Collection<CompressedEnforceMatch> matches = new ArrayList<>();
		PositionListIndex pli = columnPair.getLeftPli();
		for (Cluster cluster : pli) {
			CompressedEnforceMatch match = enforce(cluster);
			matches.add(match);
		}
		return matches;
	}

	private CompressedEnforceMatch enforce(IntCollection leftMatches, IntCollection rightMatches) {
		Iterable<int[]> left = leftRecords.getRecords(leftMatches);
		Iterable<int[]> right = rightRecords.getRecords(rightMatches);
		return new CompressedEnforceMatch(left, right);
	}

	private CompressedEnforceMatch enforce(Cluster entry) {
		int value = entry.getValue();
		IntCollection leftMatches = entry.getRecords();
		IntCollection rightMatches = columnPair.getAllSimilarRightRecords(value, threshold);
		return enforce(leftMatches, rightMatches);
	}
}
