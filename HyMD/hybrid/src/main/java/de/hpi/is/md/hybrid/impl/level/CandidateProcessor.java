package de.hpi.is.md.hybrid.impl.level;

import de.hpi.is.md.hybrid.Analyzer;
import de.hpi.is.md.util.IntArrayPair;
import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class CandidateProcessor {

	@NonNull
	private final Analyzer analyzer;
	@NonNull
	private final BatchValidator validator;
	private final ViolationHandler violationHandler = new ViolationHandler();

	Collection<IntArrayPair> getRecommendations() {
		return violationHandler.pollViolations();
	}

	Statistics validateAndAnalyze(Collection<Candidate> candidates) {
		log.debug("Will perform {} validations", Integer.valueOf(candidates.size()));
		Iterable<AnalyzeTask> results = validator.validate(candidates);
		results.forEach(violationHandler::addViolations);
		return analyzer.analyze(results);
	}

}
