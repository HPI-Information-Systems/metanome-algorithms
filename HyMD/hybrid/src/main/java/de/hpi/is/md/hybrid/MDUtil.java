package de.hpi.is.md.hybrid;

import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MDUtil {

	public static boolean isNonTrivial(MDSite lhs, MDElement rhs) {
		int rhsAttr = rhs.getId();
		return lhs.getOrDefault(rhsAttr) < rhs.getThreshold();
	}
}
