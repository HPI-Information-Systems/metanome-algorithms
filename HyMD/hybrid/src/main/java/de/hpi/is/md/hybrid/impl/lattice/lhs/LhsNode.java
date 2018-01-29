package de.hpi.is.md.hybrid.impl.lattice.lhs;

import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.LazyArray;

class LhsNode {

	private final LazyArray<LhsThresholdNode> children;
	private final ValueHolder<Boolean> notSupported = new ValueHolder<>(false);

	LhsNode(int columnPairs) {
		children = new LazyArray<>(new LhsThresholdNode[columnPairs], LhsThresholdNode::new);
	}

	void add(MDSite md, int currentLhsAttr) {
		addWith(md).add(currentLhsAttr);
	}

	boolean containsMdOrGeneralization(MDSite lhs, int currentLhsAttr) {
		return containsWith(lhs).containsMdOrGeneralization(currentLhsAttr);
	}

	private LhsAddContext addWith(MDSite lhs) {
		return LhsAddContext.builder()
			.lhs(lhs)
			.children(children)
			.notSupported(notSupported)
			.build();
	}

	private LhsContainsContext containsWith(MDSite lhs) {
		return LhsContainsContext.builder()
			.lhs(lhs)
			.children(children)
			.notSupported(notSupported)
			.build();
	}

}
