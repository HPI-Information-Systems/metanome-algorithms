package de.hpi.is.md.hybrid.impl.infer;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.MDUtil;
import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ThresholdLowerer {

	@NonNull
	private final LatticeMD latticeMd;

	public void lowerThreshold(int rhsAttr, double threshold) {
		MDElement rhs = new MDElementImpl(rhsAttr, threshold);
		lowerThreshold(rhs);
	}

	public void lowerThreshold(MDElement rhs, boolean minimal) {
		int rhsAttr = rhs.getId();
		double threshold = rhs.getThreshold();
		boolean nonTrivial = isNonTrivial(rhs);
		if (minimal && nonTrivial) {
			latticeMd.setRhs(rhsAttr, threshold);
			log.trace("{}->{} lowered threshold to {}", latticeMd.getLhs(), Integer.valueOf(rhsAttr), Double.valueOf(threshold));
		} else {
			log.trace("{}->{} removed", latticeMd.getLhs(), Integer.valueOf(rhsAttr));
			latticeMd.removeRhs(rhsAttr);
		}
	}

	private boolean isNonTrivial(MDElement rhs) {
		MDSite lhs = latticeMd.getLhs();
		return MDUtil.isNonTrivial(lhs, rhs);
	}

	private void lowerThreshold(MDElement rhs) {
		boolean minimal = latticeMd.wouldBeMinimal(rhs);
		lowerThreshold(rhs, minimal);
	}
}
