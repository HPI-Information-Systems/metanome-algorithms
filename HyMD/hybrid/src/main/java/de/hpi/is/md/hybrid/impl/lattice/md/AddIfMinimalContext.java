package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.LazyArray;
import de.hpi.is.md.util.OptionalDouble;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;

final class AddIfMinimalContext extends MDContext {

	@NonNull
	private final LazyArray<ThresholdNode> children;
	@NonNull
	private final MDSite rhs;

	@Builder
	private AddIfMinimalContext(@NonNull MD md, @NonNull LazyArray<ThresholdNode> children,
		@NonNull MDSite rhs) {
		super(md);
		this.children = children;
		this.rhs = rhs;
	}

	Optional<LhsRhsPair> addIfMinimal(int currentLhsAttr) {
		if (isMd()) {
			return Optional.empty();
		}
		return getNext(currentLhsAttr)
			.map(this::addIfMinimal)
			.orElseGet(this::addToThis);
	}

	private Optional<LhsRhsPair> addIfMinimal(MDElement next) {
		int id = next.getId();
		int nextLhsAttr = id + 1;
		boolean childContains = childContains(nextLhsAttr).booleanValue();
		if (childContains) {
			return Optional.empty();
		}
		ThresholdNode child = children.getOrCreate(id);
		double threshold = next.getThreshold();
		return child.addIfMinimal(md, nextLhsAttr, threshold);
	}

	private Optional<LhsRhsPair> addToThis() {
		MDElement mdRhs = md.getRhs();
		addToThis(mdRhs);
		MDSite lhs = md.getLhs();
		LhsRhsPair pair = toPair(lhs);
		return Optional.of(pair);
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

	private Boolean childContains(int nextLhsAttr) {
		return getNext(nextLhsAttr)
			.map(this::containsMdOrGeneralization)
			.orElse(Boolean.FALSE);
	}

	private boolean containsMdOrGeneralization(MDElement next) {
		int id = next.getId();
		int nextLhsAttrId = id + 1;
		double threshold = next.getThreshold();
		boolean childContains = children.get(id)
			.map(child -> Boolean.valueOf(child.containsMdOrGeneralization(md, nextLhsAttrId, threshold)))
			.orElse(Boolean.FALSE)
			.booleanValue();
		return childContains || containsMdOrGeneralization(nextLhsAttrId);
	}

	private boolean containsMdOrGeneralization(int currentLhsAttr) {
		if (isMd()) {
			return true;
		}
		return childContains(currentLhsAttr).booleanValue();
	}

	private OptionalDouble getThreshold() {
		int attr = md.getRhs().getId();
		return rhs.get(attr);
	}

	private double getUpdatedThreshold(int rhsAttr, double threshold) {
		return rhs.get(rhsAttr)
			.map(t -> Double.valueOf(Math.min(t, threshold)))
			.orElse(Double.valueOf(threshold))
			.doubleValue();
	}

	private boolean isAbove(double threshold) {
		return threshold >= md.getRhs().getThreshold();
	}

	private boolean isMd() {
		return getThreshold()
			.map(this::isAbove)
			.orElse(Boolean.FALSE)
			.booleanValue();
	}

	private LhsRhsPair toPair(MDSite lhs) {
		MDSite clone = lhs.clone();
		return new LhsRhsPair(clone, rhs);
	}
}
