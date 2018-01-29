package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.sim.SimilarityMeasure;
import de.hpi.is.md.util.LazyArray;
import de.hpi.is.md.util.MathUtils;
import java.util.Arrays;
import lombok.Builder;
import lombok.NonNull;

@Builder
class MaxContext {

	@NonNull
	private final MDSite lhs;
	private final int[] rhsAttrs;
	@NonNull
	private final MDSite rhs;
	@NonNull
	private final LazyArray<ThresholdNode> children;

	static boolean allMax(double[] maxThreshold) {
		return Arrays.stream(maxThreshold).allMatch(MaxContext::isMax);
	}

	private static boolean isMax(double d) {
		return d == SimilarityMeasure.MAX_SIMILARITY;
	}

	double[] getMaxThreshold(int currentLhsAttr) {
		double[] thresholdHere = getThreshold();
		if (allMax(thresholdHere)) {
			return thresholdHere;
		}
		double[] childThreshold = getChildThreshold(currentLhsAttr);
		return MathUtils.max(thresholdHere, childThreshold);
	}

	private double[] defaultThresholds() {
		return new double[rhsAttrs.length];
	}

	private double[] getChildThreshold(int currentLhsAttr) {
		return lhs.nextElement(currentLhsAttr)
			.map(this::getMaxThreshold)
			.orElseGet(this::defaultThresholds);
	}

	private double[] getMaxThreshold(MDElement next) {
		int id = next.getId();
		int nextLhsAttrId = id + 1;
		double threshold = next.getThreshold();
		double[] childThreshold = children.get(id)
			.map(child -> child.getMaxThreshold(lhs, rhsAttrs, nextLhsAttrId, threshold))
			.orElseGet(this::defaultThresholds);
		return allMax(childThreshold) ? childThreshold
			: MathUtils.max(childThreshold, getMaxThreshold(nextLhsAttrId));
	}

	private double[] getThreshold() {
		return Arrays.stream(rhsAttrs)
			.mapToDouble(rhs::getOrDefault)
			.toArray();
	}
}
