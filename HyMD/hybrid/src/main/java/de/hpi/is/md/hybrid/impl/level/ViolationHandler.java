package de.hpi.is.md.hybrid.impl.level;

import de.hpi.is.md.hybrid.ValidationResult;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.util.IntArrayPair;
import de.hpi.is.md.util.PollCollection;
import de.hpi.is.md.util.PollSet;
import java.util.Collection;

class ViolationHandler {

	private final PollCollection<IntArrayPair> violations = new PollSet<>();

	void addViolations(AnalyzeTask task) {
		ValidationResult result = task.getResult();
		Collection<RhsResult> rhsResults = result.getRhsResults();
		rhsResults.forEach(this::addViolation);
	}

	Collection<IntArrayPair> pollViolations() {
		return violations.poll();
	}

	private void addViolation(RhsResult rhsResult) {
		Collection<IntArrayPair> newViolations = rhsResult.getViolations();
		violations.addAll(newViolations);
	}
}
