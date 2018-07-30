package de.hpi.is.md.hybrid.impl.lattice;

import de.hpi.is.md.hybrid.Lattice;
import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.SimilaritySet;
import de.hpi.is.md.hybrid.impl.lattice.lhs.LhsLattice;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FullLattice {

	@NonNull
	private final Lattice lattice;
	@NonNull
	private final LhsLattice notSupported;

	public Optional<LatticeMD> addIfMinimalAndSupported(MD md) {
		MDSite lhs = md.getLhs();
		if (notSupported.containsMdOrGeneralization(lhs)) {
			return Optional.empty();
		}
		return lattice.addIfMinimal(md);
	}

	public Collection<LatticeMD> findViolated(SimilaritySet similaritySet) {
		return lattice.findViolated(similaritySet);
	}

	public int getDepth() {
		return lattice.getDepth();
	}

	public Collection<LatticeMD> getLevel(int level) {
		Collection<LatticeMD> candidates = lattice.getLevel(level);
		return StreamUtils.seq(candidates)
			.filter(this::isSupported)
			.toList();
	}

	public void markNotSupported(MDSite lhs) {
		notSupported.addIfMinimal(lhs);
	}

	public int size() {
		return lattice.size();
	}

	private boolean isSupported(LatticeMD latticeMD) {
		MDSite lhs = latticeMD.getLhs();
		return !notSupported.containsMdOrGeneralization(lhs);
	}
}
