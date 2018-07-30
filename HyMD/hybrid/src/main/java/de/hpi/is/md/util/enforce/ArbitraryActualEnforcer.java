package de.hpi.is.md.util.enforce;

import de.hpi.is.md.hybrid.impl.LhsSelector;
import de.hpi.is.md.hybrid.impl.RecordGrouper;
import de.hpi.is.md.hybrid.impl.Selector;
import it.unimi.dsi.fastutil.ints.IntCollection;
import java.util.Collection;
import lombok.Builder;
import lombok.NonNull;

@Builder
class ArbitraryActualEnforcer implements ActualEnforcer {

	@NonNull
	private final LhsSelector intersector;
	@NonNull
	private final RecordSelector rightRecords;
	@NonNull
	private final RecordGrouper grouper;

	@Override
	public Collection<CompressedEnforceMatch> enforce() {
		return grouper.buildSelectors()
			.map(t -> t.map(this::enforce))
			.toList();
	}

	private CompressedEnforceMatch enforce(Selector selector, Collection<int[]> left) {
		IntCollection rightMatches = intersector.findLhsMatches(selector);
		Iterable<int[]> right = rightRecords.getRecords(rightMatches);
		return new CompressedEnforceMatch(left, right);
	}
}
