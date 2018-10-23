package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.LazyArray;
import lombok.Builder;
import lombok.NonNull;

final class AddContext extends MDContext {

	@NonNull
	private final LazyArray<ThresholdNode> children;
	@NonNull
	private final MDSite rhs;

	@Builder
	private AddContext(@NonNull MD md, @NonNull LazyArray<ThresholdNode> children,
		@NonNull MDSite rhs) {
		super(md);
		this.children = children;
		this.rhs = rhs;
	}

	LhsRhsPair add(int currentLhsAttr) {
		return getNext(currentLhsAttr)
			.map(this::add)
			.orElseGet(this::addToThis);
	}

	private LhsRhsPair add(MDElement next) {
		int id = next.getId();
		ThresholdNode child = children.getOrCreate(id);
		int nextLhsAttr = id + 1;
		double threshold = next.getThreshold();
		return child.add(md, nextLhsAttr, threshold);
	}

	private LhsRhsPair addToThis() {
		MDElement mdRhs = md.getRhs();
		addToThis(mdRhs);
		MDSite lhs = md.getLhs();
		return toPair(lhs);
	}

	private void addToThis(MDElement element) {
		double newThreshold = element.getThreshold();
		int rhsAttr = element.getId();
		addToThis(rhsAttr, newThreshold);
	}

	private void addToThis(int rhsAttr, double newThreshold) {
		double threshold = getUpdatedThreshold(rhsAttr, newThreshold);
		rhs.set(rhsAttr, threshold);
	}

	private double getUpdatedThreshold(int rhsAttr, double threshold) {
		return rhs.get(rhsAttr)
			.map(t -> Double.valueOf(Math.min(t, threshold)))
			.orElse(Double.valueOf(threshold))
			.doubleValue();
	}

	private LhsRhsPair toPair(MDSite lhs) {
		MDSite clone = lhs.clone();
		return new LhsRhsPair(clone, rhs);
	}
}
