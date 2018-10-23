package de.hpi.is.md.hybrid.impl.lattice.lhs;

import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.LazyArray;
import lombok.Builder;
import lombok.NonNull;

final class LhsContainsContext extends LhsContext {

	@NonNull
	private final ValueHolder<Boolean> notSupported;
	@NonNull
	private final LazyArray<LhsThresholdNode> children;

	@Builder
	private LhsContainsContext(@NonNull MDSite lhs, ValueHolder<Boolean> notSupported,
		@NonNull LazyArray<LhsThresholdNode> children) {
		super(lhs);
		this.notSupported = notSupported;
		this.children = children;
	}

	boolean containsMdOrGeneralization(int currentLhsAttr) {
		if (isNotSupported()) {
			return true;
		}
		return getNext(currentLhsAttr)
			.map(this::containsMdOrGeneralization)
			.orElse(Boolean.FALSE)
			.booleanValue();
	}

	private boolean containsMdOrGeneralization(MDElement next) {
		int id = next.getId();
		int nextLhsAttrId = id + 1;
		double threshold = next.getThreshold();
		boolean childContains = children.get(id)
			.map(child -> Boolean.valueOf(child.containsMdOrGeneralization(lhs, nextLhsAttrId, threshold)))
			.orElse(Boolean.FALSE)
			.booleanValue();
		return childContains || containsMdOrGeneralization(nextLhsAttrId);
	}

	private boolean isNotSupported() {
		return notSupported.getValue().booleanValue();
	}

}
