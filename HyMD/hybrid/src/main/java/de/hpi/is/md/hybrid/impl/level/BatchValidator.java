package de.hpi.is.md.hybrid.impl.level;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.Rhs;
import de.hpi.is.md.hybrid.Validator;
import de.hpi.is.md.hybrid.impl.infer.ThresholdLowerer;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class BatchValidator {

	@NonNull
	private final Validator validator;
	private final boolean parallel;

	Iterable<AnalyzeTask> validate(Iterable<Candidate> candidates) {
		return StreamUtils.stream(candidates, parallel)
			.map(this::createTask)
			.map(ValidationTask::validate)
			.collect(Collectors.toList());
	}

	private ValidationTask createTask(Candidate candidate) {
		LatticeMD latticeMd = candidate.getLatticeMd();
		Collection<Rhs> rhs = candidate.getRhs();
		MDSite lhs = latticeMd.getLhs();
		ThresholdLowerer lowerer = new ThresholdLowerer(latticeMd);
		return ValidationTask.builder()
			.validator(validator)
			.lhs(lhs)
			.lowerer(lowerer)
			.rhs(rhs)
			.build();
	}

}
