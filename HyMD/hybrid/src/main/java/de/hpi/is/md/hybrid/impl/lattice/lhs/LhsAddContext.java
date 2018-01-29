package de.hpi.is.md.hybrid.impl.lattice.lhs;

import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.LazyArray;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;

final class LhsAddContext extends LhsContext {

	@NonNull
	private final LazyArray<LhsThresholdNode> children;
	@NonNull
	private final ValueHolder<Boolean> notSupported;

	@Builder
	private LhsAddContext(@NonNull MDSite lhs,
		@NonNull LazyArray<LhsThresholdNode> children,
		@NonNull ValueHolder<Boolean> notSupported) {
		super(lhs);
		this.children = children;
		this.notSupported = notSupported;
	}

	void add(int currentLhsAttr) {
		Optional<MDElement> next = getNext(currentLhsAttr);
		if (next.isPresent()) {
			next.ifPresent(this::add);
		} else {
			addToThis();
		}
	}

	private void add(MDElement next) {
		int id = next.getId();
		LhsThresholdNode child = children.getOrCreate(id);
		int nextLhsAttr = id + 1;
		double threshold = next.getThreshold();
		child.add(lhs, nextLhsAttr, threshold);
	}

	private void addToThis() {
		notSupported.setValue(true);
	}
}
