package de.hpi.is.md.hybrid;

import de.hpi.is.md.hybrid.impl.lattice.md.LatticeImpl;
import de.hpi.is.md.hybrid.impl.lattice.md.LevelFunction;
import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.impl.md.MDImpl;
import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.sim.SimilarityMeasure;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class LatticeHelper {

	static Lattice createLattice(LevelFunction levelFunction) {
		int columnPairs = levelFunction.size();
		Lattice lattice = new LatticeImpl(levelFunction);
		MDSite lhs = new MDSiteImpl(columnPairs);
		for (int rhsAttr = 0; rhsAttr < columnPairs; rhsAttr++) {
			MD md = createMD(lhs, rhsAttr);
			lattice.add(md);
		}
		return lattice;
	}

	private static MDElement createInitialRhs(int rhsAttr) {
		return new MDElementImpl(rhsAttr, SimilarityMeasure.MAX_SIMILARITY);
	}

	private static MD createMD(MDSite lhs, int rhsAttr) {
		MDElement rhs = createInitialRhs(rhsAttr);
		return new MDImpl(lhs, rhs);
	}
}