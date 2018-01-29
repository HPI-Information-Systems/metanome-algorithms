package de.hpi.is.md.hybrid.impl.lattice.lhs;

import com.codahale.metrics.annotation.Timed;
import de.hpi.is.md.hybrid.md.MDSite;

//@Metrics
public class LhsLattice {

	private final LhsNode root;
	private int depth = 0;

	public LhsLattice(int columnPairs) {
		this.root = new LhsNode(columnPairs);
	}

	public void addIfMinimal(MDSite lhs) {
		if (!containsMdOrGeneralization(lhs)) {
			add(lhs);
		}
	}

	@Timed
	public boolean containsMdOrGeneralization(MDSite lhs) {
		return root.containsMdOrGeneralization(lhs, 0);
	}

	@Timed
	private void add(MDSite lhs) {
		depth = Math.max(lhs.cardinality(), depth);
		root.add(lhs, 0);
	}

}
