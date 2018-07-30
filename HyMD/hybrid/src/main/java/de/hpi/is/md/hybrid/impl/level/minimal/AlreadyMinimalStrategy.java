package de.hpi.is.md.hybrid.impl.level.minimal;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.impl.lattice.FullLattice;
import de.hpi.is.md.hybrid.impl.level.Candidate;
import de.hpi.is.md.hybrid.impl.level.LevelStrategy;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class AlreadyMinimalStrategy implements LevelStrategy {

	@NonNull
	private final FullLattice fullLattice;
	@NonNull
	private final CandidateBuilder candidateBuilder;
	private int currentLevel = 0;

	public static LevelStrategy of(FullLattice fullLattice, double minThreshold) {
		CandidateBuilder candidateBuilder = new CandidateBuilder(minThreshold);
		return new AlreadyMinimalStrategy(fullLattice, candidateBuilder);
	}

	@Override
	public boolean areLevelsLeft() {
		return currentLevel <= fullLattice.getDepth();
	}

	@Override
	public Collection<Candidate> getCurrentLevel() {
		log.debug("Retrieved level {}", currentLevel);
		Collection<LatticeMD> mds = fullLattice.getLevel(currentLevel);
		Collection<Candidate> candidates = candidateBuilder.toCandidates(mds);
		currentLevel++;
		return candidates;
	}
}
