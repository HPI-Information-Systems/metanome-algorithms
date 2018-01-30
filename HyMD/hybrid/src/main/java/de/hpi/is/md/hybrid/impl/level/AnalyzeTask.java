package de.hpi.is.md.hybrid.impl.level;

import de.hpi.is.md.hybrid.ValidationResult;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.hybrid.impl.infer.ThresholdLowerer;
import de.hpi.is.md.hybrid.md.MDElement;
import java.util.Collection;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AnalyzeTask {

	@Getter
	@NonNull
	private final ValidationResult result;
	@NonNull
	private final ThresholdLowerer lowerer;

	public void lower() {
		Collection<RhsResult> rhsResults = result.getRhsResults();
		rhsResults.forEach(this::lower);
	}

	private void lower(RhsResult rhsResult) {
		MDElement rhs = rhsResult.getActual();
		boolean validAndMinimal = rhsResult.isValidAndMinimal();
		lowerer.lowerThreshold(rhs, validAndMinimal);
	}
}
