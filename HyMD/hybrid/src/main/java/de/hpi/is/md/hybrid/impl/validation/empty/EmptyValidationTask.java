package de.hpi.is.md.hybrid.impl.validation.empty;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.ValidationResult;
import de.hpi.is.md.hybrid.ValidationResult.LhsResult;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.hybrid.impl.validation.ValidationTask;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import lombok.Builder;
import lombok.NonNull;

@Builder
class EmptyValidationTask implements ValidationTask {

	@NonNull
	private final Collection<RhsValidationTask> rhs;
	@NonNull
	private final MDSite lhs;
	@NonNull
	private final DictionaryRecords leftRecords;
	@NonNull
	private final DictionaryRecords rightRecords;

	@Override
	public ValidationResult validate() {
		long support = (long) leftRecords.getAll().size() * rightRecords.getAll().size();
		LhsResult lhsResult = new LhsResult(lhs, support);
		Collection<RhsResult> rhsResults = getResults();
		return new ValidationResult(lhsResult, rhsResults);
	}

	private Collection<RhsResult> getResults() {
		return StreamUtils.seq(rhs)
			.map(RhsValidationTask::createResult)
			.toList();
	}
}
