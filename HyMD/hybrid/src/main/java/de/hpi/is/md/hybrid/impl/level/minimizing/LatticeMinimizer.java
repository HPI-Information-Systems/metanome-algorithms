package de.hpi.is.md.hybrid.impl.level.minimizing;

import de.hpi.is.md.hybrid.impl.lattice.candidate.CandidateLattice;
import de.hpi.is.md.hybrid.impl.level.Candidate;
import java.util.Collection;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LatticeMinimizer implements Minimizer {

	private final int size;

	@Override
	public Collection<Candidate> toCandidates(Iterable<IntermediateCandidate> preCandidates) {
		return withLattice().minimize(preCandidates);
	}

	private WithLattice withLattice() {
		return new WithLattice();
	}

	private class WithLattice {

		private final CandidateLattice lattice = new CandidateLattice(size);

		private void add(IntermediateCandidate intermediateCandidate) {
			intermediateCandidate.forEach(lattice::addIfMinimal);
		}

		private Collection<Candidate> minimize(Iterable<IntermediateCandidate> preCandidates) {
			preCandidates.forEach(this::add);
			return lattice.getAll();
		}
	}
}
