package de.hpi.is.md.hybrid.impl.level.minimizing;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.impl.level.Candidate;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class CandidateBuilder {

	private final double minThreshold;
	@NonNull
	private final Minimizer minimizer;
	private final Multimap<MDSite, Integer> validated = HashMultimap.create();

	void clear() {
		validated.clear();
	}

	Collection<Candidate> toCandidates(Iterable<LatticeMD> latticeMDs) {
		Collection<IntermediateCandidate> preCandidates = toIntermediateCandidates(latticeMDs);
		Collection<Candidate> candidates = minimizer.toCandidates(preCandidates);
		candidates.forEach(this::addToValidated);
		return candidates;
	}

	private void addToValidated(Candidate candidate) {
		candidate.forEach(this::addToValidated);
	}

	private void addToValidated(MDSite lhs, MDElement rhs) {
		int rhsAttr = rhs.getId();
		validated.put(lhs, Integer.valueOf(rhsAttr));
	}

	private IntermediateCandidate toCandidate(LatticeMD latticeMd) {
		return with(latticeMd).toCandidate();
	}

	private Collection<IntermediateCandidate> toIntermediateCandidates(
		Iterable<LatticeMD> latticeMDs) {
		return StreamUtils.seq(latticeMDs)
			.map(this::toCandidate)
			.filter(IntermediateCandidate::isNotEmpty)
			.toList();
	}

	private WithLatticeMD with(LatticeMD latticeMd) {
		return new WithLatticeMD(latticeMd);
	}

	@RequiredArgsConstructor
	private class WithLatticeMD {

		@NonNull
		private final LatticeMD latticeMd;

		private Collection<MDElement> getRemainingMdElements() {
			Iterable<MDElement> rhs = latticeMd.getRhs();
			return StreamUtils.seq(rhs)
				.filter(this::isValid)
				.filter(this::notValidated)
				.toList();
		}

		private boolean isValid(MDElement rhs) {
			double threshold = rhs.getThreshold();
			return threshold >= minThreshold;
		}

		private boolean notValidated(MDElement rhs) {
			MDSite lhs = latticeMd.getLhs();
			int rhsAttr = rhs.getId();
			return !validated.containsEntry(lhs, Integer.valueOf(rhsAttr));
		}

		private IntermediateCandidate toCandidate() {
			Collection<MDElement> remaining = getRemainingMdElements();
			return new IntermediateCandidate(latticeMd, remaining);
		}
	}
}
