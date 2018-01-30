package de.hpi.is.md.hybrid.impl.level;


import de.hpi.is.md.hybrid.Rhs;
import de.hpi.is.md.hybrid.ValidationResult;
import de.hpi.is.md.hybrid.Validator;
import de.hpi.is.md.hybrid.impl.infer.ThresholdLowerer;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Collection;
import lombok.Builder;
import lombok.NonNull;

@Builder
class ValidationTask {

	@NonNull
	private final Validator validator;
	@NonNull
	private final MDSite lhs;
	@NonNull
	private final ThresholdLowerer lowerer;
	@NonNull
	private final Collection<Rhs> rhs;

	AnalyzeTask validate() {
		ValidationResult results = validator.validate(lhs, rhs);
		return new AnalyzeTask(results, lowerer);
	}
}
