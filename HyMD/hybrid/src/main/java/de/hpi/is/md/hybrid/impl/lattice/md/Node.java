package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.LazyArray;
import java.util.Optional;
import lombok.Getter;
import lombok.ToString;

@ToString(of = "rhs")
class Node {

	@Getter
	private final LazyArray<ThresholdNode> children;
	@Getter
	private final MDSite rhs;

	Node(int columnPairs) {
		children = new LazyArray<>(new ThresholdNode[columnPairs], ThresholdNode::new);
		rhs = new MDSiteImpl(columnPairs);
	}

	Optional<LhsRhsPair> addIfMinimal(MD md, int currentLhsAttr) {
		return addIfMinimalWith(md).addIfMinimal(currentLhsAttr);
	}

	LhsRhsPair add(MD md, int currentLhsAttr) {
		return addWith(md).add(currentLhsAttr);
	}

	boolean containsMdOrGeneralization(MD md, int currentLhsAttr) {
		return containsWith(md).containsMdOrGeneralization(currentLhsAttr);
	}

	double[] getMaxThreshold(MDSite lhs, int[] rhsAttrs, int currentLhsAttr) {
		return maxWith(lhs, rhsAttrs).getMaxThreshold(currentLhsAttr);
	}

	private AddContext addWith(MD md) {
		return AddContext.builder()
			.md(md)
			.children(children)
			.rhs(rhs)
			.build();
	}

	private AddIfMinimalContext addIfMinimalWith(MD md) {
		return AddIfMinimalContext.builder()
			.md(md)
			.children(children)
			.rhs(rhs)
			.build();
	}

	private ContainsContext containsWith(MD md) {
		return ContainsContext.builder()
			.md(md)
			.children(children)
			.rhs(rhs)
			.build();
	}

	private MaxContext maxWith(MDSite lhs, int[] rhsAttrs) {
		return MaxContext.builder()
			.children(children)
			.lhs(lhs)
			.rhs(rhs)
			.rhsAttrs(rhsAttrs)
			.build();
	}

}
