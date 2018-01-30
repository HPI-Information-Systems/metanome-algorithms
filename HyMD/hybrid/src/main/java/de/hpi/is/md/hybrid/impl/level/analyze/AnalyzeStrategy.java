package de.hpi.is.md.hybrid.impl.level.analyze;

import de.hpi.is.md.hybrid.ValidationResult.LhsResult;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.hybrid.impl.level.Statistics;

public interface AnalyzeStrategy {

	void deduce(RhsResult rhsResult);

	Statistics getStatistics();

	interface Factory {

		AnalyzeStrategy create(LhsResult lhsResult);

	}
}
