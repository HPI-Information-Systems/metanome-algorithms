package de.hpi.is.md.hybrid.impl.level.analyze;

import de.hpi.is.md.hybrid.ValidationResult.LhsResult;
import de.hpi.is.md.hybrid.impl.level.analyze.AnalyzeStrategy.Factory;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class SupportBasedFactory implements Factory {

	private final long minSupport;
	@NonNull
	private final Factory supportedFactory;
	@NonNull
	private final Factory notSupportedFactory;

	@Override
	public AnalyzeStrategy create(LhsResult lhsResult) {
		if (isSupported(lhsResult)) {
			return supportedFactory.create(lhsResult);
		}
		return notSupportedFactory.create(lhsResult);
	}

	private boolean isSupported(LhsResult lhsResult) {
		long support = lhsResult.getSupport();
		return support >= minSupport;
	}
}
