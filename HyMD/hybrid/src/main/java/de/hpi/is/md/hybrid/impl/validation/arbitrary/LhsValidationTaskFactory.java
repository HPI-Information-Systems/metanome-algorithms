package de.hpi.is.md.hybrid.impl.validation.arbitrary;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.impl.ColumnPairWithThreshold;
import de.hpi.is.md.hybrid.impl.LhsSelector;
import de.hpi.is.md.hybrid.impl.RecordGrouper;
import de.hpi.is.md.hybrid.impl.validation.RhsValidationTask;
import de.hpi.is.md.hybrid.impl.validation.ValidationTask;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Collection;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;

@Builder
class LhsValidationTaskFactory {

	@NonNull
	private DictionaryRecords leftRecords;
	@NonNull
	private DictionaryRecords rightRecords;
	private long minSupport;

	ValidationTask create(Collection<ColumnPairWithThreshold> lhsPairs,
		Collection<RhsValidationTask> rhsTasks, MDSite lhs, List<PreprocessedColumnPair> columnPairs) {
		LhsSelector selector = new LhsSelector(lhsPairs, rightRecords);
		RecordGrouper grouper = RecordGrouper.create(columnPairs, leftRecords);
		return new ArbitraryValidationTask(selector, rhsTasks, grouper, lhs, minSupport);
	}

}
