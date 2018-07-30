package de.hpi.is.md.hybrid.impl.level.minimizing;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.impl.level.Candidate;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.Optionals;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class SimpleMinimizer implements Minimizer {

	@Override
	public Collection<Candidate> toCandidates(Iterable<IntermediateCandidate> preCandidates) {
		MinimalCandidateFilter minimalCandidateFilter = new MinimalCandidateFilter(preCandidates);
		return StreamUtils.seq(preCandidates)
			.map(minimalCandidateFilter::asMinimal)
			.flatMap(Optionals::stream)
			.toList();
	}

	@RequiredArgsConstructor
	private static class MinimalCandidateFilter {

		@NonNull
		private final Iterable<IntermediateCandidate> candidates;

		private Optional<Candidate> asMinimal(IntermediateCandidate intermediateCandidate) {
			LatticeMD latticeMd = intermediateCandidate.getLatticeMd();
			Collection<MDElement> minimalRhs = getMinimalRhs(intermediateCandidate);
			return Optionals.of(minimalRhs)
				.map(rhs -> new Candidate(latticeMd, rhs));
		}

		private Collection<MDElement> getMinimalRhs(IntermediateCandidate candidate) {
			LatticeMD latticeMd = candidate.getLatticeMd();
			MDSite lhs = latticeMd.getLhs();
			Collection<MDElement> rhs = candidate.getRhs();
			MinimalRhsFilter task = MinimalRhsFilter.create(lhs, rhs);
			return task.asMinimal(candidates);
		}

	}
}
