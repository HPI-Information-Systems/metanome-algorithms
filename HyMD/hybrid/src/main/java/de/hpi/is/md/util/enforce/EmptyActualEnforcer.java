package de.hpi.is.md.util.enforce;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.Collections;
import lombok.Builder;
import lombok.NonNull;

@Builder
class EmptyActualEnforcer implements ActualEnforcer {

	@NonNull
	private final DictionaryRecords leftRecords;
	@NonNull
	private final DictionaryRecords rightRecords;

	@Override
	public Collection<CompressedEnforceMatch> enforce() {
		Iterable<int[]> left = StreamUtils.seq(leftRecords).toList();
		Iterable<int[]> right = StreamUtils.seq(rightRecords).toList();
		CompressedEnforceMatch match = new CompressedEnforceMatch(left, right);
		return Collections.singletonList(match);
	}
}
