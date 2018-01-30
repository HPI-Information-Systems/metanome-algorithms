package de.hpi.is.md.hybrid.impl.lattice.candidate;

import com.codahale.metrics.annotation.Timed;
import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.impl.level.Candidate;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//@Metrics
public class CandidateLattice {

	private final CandidateNode root;

	public CandidateLattice(int columnPairs) {
		this.root = new CandidateNode(columnPairs);
	}

	public void addIfMinimal(LatticeMD latticeMD, MDElement rhs) {
		MDSite lhs = latticeMD.getLhs();
		int rhsAttr = rhs.getId();
		if (!containsMdOrGeneralization(lhs, rhsAttr)) {
			add(latticeMD, rhs);
			removeSpecializations(lhs, rhsAttr);
		}
	}

	public Collection<Candidate> getAll() {
		List<Candidate> results = new ArrayList<>();
		root.getAll(results);
		return results;
	}

	@Timed
	private void add(LatticeMD latticeMD, MDElement rhs) {
		root.add(latticeMD, rhs, 0);
	}

	@Timed
	private boolean containsMdOrGeneralization(MDSite lhs, int rhsAttr) {
		return root.containsMdOrGeneralization(lhs, rhsAttr, 0);
	}

	private void removeSpecializations(MDSite lhs, int rhsAttr) {
		root.removeSpecializations(lhs, rhsAttr, 0, false);
	}

}
