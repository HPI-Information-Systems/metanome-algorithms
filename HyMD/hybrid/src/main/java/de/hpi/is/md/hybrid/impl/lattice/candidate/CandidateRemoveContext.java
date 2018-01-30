package de.hpi.is.md.hybrid.impl.lattice.candidate;

import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.LazyArray;
import lombok.Builder;
import lombok.NonNull;

final class CandidateRemoveContext extends LhsContext {

	@NonNull
	private final MDSite rhs;
	@NonNull
	private final LazyArray<CandidateThresholdNode> children;
	private final int rhsAttr;
	private final boolean specialized;

	@Builder
	private CandidateRemoveContext(@NonNull MDSite lhs, @NonNull int rhsAttr,
		@NonNull MDSite rhs,
		boolean specialized,
		@NonNull LazyArray<CandidateThresholdNode> children) {
		super(lhs);
		this.rhs = rhs;
		this.specialized = specialized;
		this.rhsAttr = rhsAttr;
		this.children = children;
	}

	void remove(int currentLhsAttr) {
		if (specialized && !getNext(currentLhsAttr).isPresent()) {
			removeHere();
			return;
		}
		removeNoCheck(currentLhsAttr);
	}

	private void removeHere() {
		rhs.clear(rhsAttr);
	}

	private void removeNoCheck(int initial) {
		int maxChild = getNext(initial)
			.map(MDElement::getId)
			.map(i -> i + 1)
			.orElseGet(children::size);
		for (int currentLhsAttr = initial; currentLhsAttr < maxChild; currentLhsAttr++) {
			double threshold = lhs.getOrDefault(currentLhsAttr);
			int nextLhsAttrId = currentLhsAttr + 1;
			children.get(currentLhsAttr)
				.ifPresent(
					child -> child.remove(lhs, rhsAttr, nextLhsAttrId, threshold, specialized));
		}
	}

}
