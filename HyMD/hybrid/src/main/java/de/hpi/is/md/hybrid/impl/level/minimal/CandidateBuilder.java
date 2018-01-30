package de.hpi.is.md.hybrid.impl.level.minimal;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.impl.level.Candidate;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.util.Optionals;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class CandidateBuilder {

	private final double minThreshold;

	Collection<Candidate> toCandidates(Iterable<LatticeMD> latticeMDs) {
		return StreamUtils.seq(latticeMDs)
			.map(this::toCandidate)
			.flatMap(Optionals::stream)
			.toList();
	}

	private Optional<Candidate> toCandidate(LatticeMD latticeMD) {
		return with(latticeMD).toCandidate();
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
				.toList();
		}

		private boolean isValid(MDElement rhs) {
			double threshold = rhs.getThreshold();
			return threshold >= minThreshold;
		}

		private Optional<Candidate> toCandidate() {
			Collection<MDElement> remaining = getRemainingMdElements();
			return Optionals.of(remaining)
				.map(rhs -> new Candidate(latticeMd, rhs));
		}
	}
}
