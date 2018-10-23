package de.hpi.is.md.hybrid.impl.level.minimizing;

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
public final class MinimizingLevelStrategy implements LevelStrategy {

	@NonNull
	private final FullLattice fullLattice;
	@NonNull
	private final CandidateBuilder candidateBuilder;
	private int currentLevel = 0;

	public static LevelStrategy of(FullLattice fullLattice, double minThreshold) {
		Minimizer minimizer = new SimpleMinimizer();
		CandidateBuilder candidateBuilder = new CandidateBuilder(minThreshold, minimizer);
		return new MinimizingLevelStrategy(fullLattice, candidateBuilder);
	}

	@Override
	public boolean areLevelsLeft() {
		return currentLevel <= fullLattice.getDepth();
	}

	@Override
	public Collection<Candidate> getCurrentLevel() {
		log.debug("Retrieved level {}", Integer.valueOf(currentLevel));
		Collection<LatticeMD> mds = fullLattice.getLevel(currentLevel);
		Collection<Candidate> candidates = candidateBuilder.toCandidates(mds);
		if (candidates.isEmpty()) {
			finishLevel();
		}
		return candidates;
	}

	private void finishLevel() {
		log.debug("Finished level {}", Integer.valueOf(currentLevel));
		currentLevel++;
		candidateBuilder.clear();
	}
}
