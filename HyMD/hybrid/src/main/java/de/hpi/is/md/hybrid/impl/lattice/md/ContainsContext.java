package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.LazyArray;
import de.hpi.is.md.util.OptionalDouble;
import lombok.Builder;
import lombok.NonNull;

final class ContainsContext extends MDContext {

	@NonNull
	private final MDSite rhs;
	@NonNull
	private final LazyArray<ThresholdNode> children;

	@Builder
	private ContainsContext(@NonNull MD md, @NonNull MDSite rhs,
		@NonNull LazyArray<ThresholdNode> children) {
		super(md);
		this.rhs = rhs;
		this.children = children;
	}

	boolean containsMdOrGeneralization(int currentLhsAttr) {
		if (isMd()) {
			return true;
		}
		return getNext(currentLhsAttr)
			.map(this::containsMdOrGeneralization)
			.orElse(false);
	}

	private boolean containsMdOrGeneralization(MDElement next) {
		int id = next.getId();
		int nextLhsAttrId = id + 1;
		double threshold = next.getThreshold();
		boolean childContains = children.get(id)
			.map(child -> child.containsMdOrGeneralization(md, nextLhsAttrId, threshold))
			.orElse(false);
		return childContains || containsMdOrGeneralization(nextLhsAttrId);
	}

	private OptionalDouble getThreshold() {
		int attr = md.getRhs().getId();
		return rhs.get(attr);
	}

	private boolean isAbove(double threshold) {
		return threshold >= md.getRhs().getThreshold();
	}

	private boolean isMd() {
		return getThreshold()
			.map(this::isAbove)
			.orElse(false);
	}

}
