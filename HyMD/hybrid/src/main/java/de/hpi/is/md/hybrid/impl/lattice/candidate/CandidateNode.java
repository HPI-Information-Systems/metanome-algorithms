package de.hpi.is.md.hybrid.impl.lattice.candidate;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.impl.level.Candidate;
import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.LazyArray;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.List;
import lombok.Setter;

class CandidateNode {

	private final LazyArray<CandidateThresholdNode> children;
	private final MDSite rhs;
	@Setter
	private LatticeMD latticeMD;

	CandidateNode(int columnPairs) {
		this.children = new LazyArray<>(new CandidateThresholdNode[columnPairs],
			CandidateThresholdNode::new);
		this.rhs = new MDSiteImpl(columnPairs);
	}

	void add(LatticeMD md, MDElement rhsElem, int currentLhsAttr) {
		addWith(md, rhsElem).add(currentLhsAttr);
	}

	boolean containsMdOrGeneralization(MDSite lhs, int rhsAttr, int currentLhsAttr) {
		return containsWith(lhs, rhsAttr).containsMdOrGeneralization(currentLhsAttr);
	}

	void getAll(List<Candidate> results) {
		if (rhs.isNotEmpty()) {
			results.add(toCandidate());
		}
		children.forEach((__, node) -> node.getAll(results));
	}

	void removeSpecializations(MDSite lhs, int rhsAttr, int currentLhsAttr, boolean specialized) {
		removeWith(lhs, rhsAttr, specialized).remove(currentLhsAttr);
	}

	private CandidateAddContext addWith(LatticeMD md, MDElement rhsElem) {
		return CandidateAddContext.builder()
			.latticeMD(md)
			.rhsElem(rhsElem)
			.rhs(rhs)
			.children(children)
			.callback(this::setLatticeMD)
			.build();
	}

	private CandidateContainsContext containsWith(MDSite lhs, int rhsAttr) {
		return CandidateContainsContext.builder()
			.lhs(lhs)
			.children(children)
			.rhsAttr(rhsAttr)
			.rhs(rhs)
			.build();
	}

	private CandidateRemoveContext removeWith(MDSite lhs, int rhsAttr, boolean specialized) {
		return CandidateRemoveContext.builder()
			.lhs(lhs)
			.children(children)
			.rhs(rhs)
			.rhsAttr(rhsAttr)
			.specialized(specialized)
			.build();
	}

	private Candidate toCandidate() {
		Collection<MDElement> collectedRhs = StreamUtils.seq(rhs).toList();
		return new Candidate(latticeMD, collectedRhs);
	}

}
