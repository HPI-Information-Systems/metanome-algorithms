package de.hpi.is.md.hybrid.impl.level.minimizing;

import static com.google.common.base.Preconditions.checkArgument;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class MinimalRhsFilter {

	@NonNull
	private final MDSite lhs;
	@NonNull
	private final Map<Integer, MDElement> fullRhs;

	static MinimalRhsFilter create(MDSite lhs, Iterable<MDElement> rhs) {
		Map<Integer, MDElement> fullRhs = StreamUtils.seq(rhs)
			.toMap(MDElement::getId);
		return new MinimalRhsFilter(lhs, fullRhs);
	}

	Collection<MDElement> asMinimal(Iterable<IntermediateCandidate> candidates) {
		candidates.forEach(this::removeNonMinimalRhs);
		return fullRhs.values();
	}

	private boolean isLhsMinimal(MDSite lhs2) {
		checkArgument(lhs.size() == lhs2.size(), "MDSites with different size");
		return lhs.equals(lhs2) || StreamUtils.seq(lhs2).anyMatch(this::isLhsMinimal);
	}

	private boolean isLhsMinimal(MDElement mdElement) {
		int attr = mdElement.getId();
		double threshold1 = lhs.getOrDefault(attr);
		double threshold2 = mdElement.getThreshold();
		return threshold2 > threshold1;
	}

	private void removeNonMinimalRhs(IntermediateCandidate preCandidate) {
		LatticeMD latticeMd = preCandidate.getLatticeMd();
		MDSite lhs = latticeMd.getLhs();
		if (isLhsMinimal(lhs)) {
			return;
		}
		Collection<MDElement> rhs = preCandidate.getRhs();
		removeNonMinimalRhs(rhs);
	}

	private void removeNonMinimalRhs(Iterable<MDElement> rhs) {
		Collection<Integer> rhsIds = StreamUtils.seq(rhs)
			.map(MDElement::getId)
			.toSet();
		rhsIds.forEach(fullRhs::remove);
	}
}
