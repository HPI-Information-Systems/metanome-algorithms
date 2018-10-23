package de.hpi.is.md.hybrid.impl.lattice.candidate;

import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.LazyArray;
import lombok.Builder;
import lombok.NonNull;

final class CandidateContainsContext extends LhsContext {

	@NonNull
	private final MDSite rhs;
	@NonNull
	private final LazyArray<CandidateThresholdNode> children;
	
	private final int rhsAttr;

	@Builder
	private CandidateContainsContext(@NonNull MDSite lhs, int rhsAttr,
		@NonNull MDSite rhs,
		@NonNull LazyArray<CandidateThresholdNode> children) {
		super(lhs);
		this.rhs = rhs;
		this.rhsAttr = rhsAttr;
		this.children = children;
	}

	boolean containsMdOrGeneralization(int currentLhsAttr) {
		return isContainedHere() || containsNoCheck(currentLhsAttr);
	}

	private boolean containsMdOrGeneralization(MDElement next) {
		int id = next.getId();
		int nextLhsAttrId = id + 1;
		double threshold = next.getThreshold();
		boolean childContains = children.get(id)
			.map(child -> Boolean.valueOf(child.containsMdOrGeneralization(lhs, rhsAttr, nextLhsAttrId, threshold)))
			.orElse(Boolean.FALSE)
			.booleanValue();
		return childContains || containsNoCheck(nextLhsAttrId);
	}

	private boolean containsNoCheck(int currentLhsAttr) {
		return getNext(currentLhsAttr)
			.map(this::containsMdOrGeneralization)
			.orElse(Boolean.FALSE)
			.booleanValue();
	}

	private boolean isContainedHere() {
		return rhs.isSet(rhsAttr);
	}

}
