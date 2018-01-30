package de.hpi.is.md.hybrid.impl.infer;

import de.hpi.is.md.hybrid.md.MDSite;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class LhsModifier {

	@NonNull
	private final MDSite lhs;

	MDSite newLhs(int attr, double threshold) {
		MDSite clone = lhs.clone();
		return clone.set(attr, threshold);
	}
}
