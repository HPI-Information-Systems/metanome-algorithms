package de.hpi.is.md.hybrid;

import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Collection;
import java.util.Optional;

public interface Lattice {

	LatticeMD add(MD md);

	default Optional<LatticeMD> addIfMinimal(MD md) {
		if (!containsMdOrGeneralization(md)) {
			LatticeMD latticeMd = add(md);
			return Optional.of(latticeMd);
		}
		return Optional.empty();
	}

	boolean containsMdOrGeneralization(MD md);

	Collection<LatticeMD> findViolated(SimilaritySet similaritySet);

	int getDepth();

	Collection<LatticeMD> getLevel(int level);

	double[] getMaxThresholds(MDSite lhs, int[] rhsAttrs);

	int size();

	interface LatticeMD {

		MDSite getLhs();

		double[] getMaxGenThresholds(int[] rhsAttrs);

		Iterable<MDElement> getRhs();

		void removeRhs(int attr);

		void setRhs(int attr, double threshold);

		boolean wouldBeMinimal(MDElement rhs);
	}
}
