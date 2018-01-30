package de.hpi.is.md.hybrid.impl.level;

import de.hpi.is.md.hybrid.impl.lattice.FullLattice;
import java.util.Collection;

public interface LevelStrategy {

	boolean areLevelsLeft();

	Collection<Candidate> getCurrentLevel();

	interface Factory {

		LevelStrategy create(FullLattice fullLattice, double minThreshold);
	}
}
