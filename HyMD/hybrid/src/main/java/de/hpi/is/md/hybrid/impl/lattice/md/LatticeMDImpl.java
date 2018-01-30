package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.hybrid.Lattice;
import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.impl.md.MDImpl;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
class LatticeMDImpl implements LatticeMD {

	@Getter
	@NonNull
	private final MDSite lhs;
	@Getter
	@NonNull
	private final MDSite rhs;
	@NonNull
	private final Lattice lattice;

	@Override
	public double[] getMaxGenThresholds(int[] rhsAttrs) {
		MDSiteContext context = getRhsContext();
		return context.with(rhsAttrs)
			.disableAndDo(() -> getMaxThresholds(rhsAttrs));
	}

	@Override
	public void removeRhs(int attr) {
		rhs.clear(attr);
	}

	@Override
	public void setRhs(int attr, double threshold) {
		rhs.set(attr, threshold);
	}

	@Override
	public boolean wouldBeMinimal(MDElement element) {
		int rhsAttr = element.getId();
		MDSiteContext context = getRhsContext();
		return context.with(new int[]{rhsAttr})
			.disableAndDo(() -> isMinimal(element));
	}

	@Override
	public String toString() {
		return lhs + "->" + rhs;
	}

	private double[] getMaxThresholds(int[] rhsAttrs) {
		return lattice.getMaxThresholds(lhs, rhsAttrs);
	}

	private MDSiteContext getRhsContext() {
		return new MDSiteContext(rhs);
	}

	private boolean isMinimal(MDElement element) {
		MD md = toMd(element);
		return !lattice.containsMdOrGeneralization(md);
	}

	private MD toMd(MDElement element) {
		return new MDImpl(lhs, element);
	}

}
