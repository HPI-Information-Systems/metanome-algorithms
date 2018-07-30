package de.hpi.is.md.hybrid.impl.level;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.Rhs;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class Candidate {

	@Getter
	@NonNull
	private final LatticeMD latticeMd;
	@NonNull
	private final Collection<MDElement> rhs;

	private static Rhs toRhs(MDElement element, double lowerBound) {
		int attr = element.getId();
		double threshold = element.getThreshold();
		return Rhs.builder()
			.rhsAttr(attr)
			.threshold(threshold)
			.lowerBound(lowerBound)
			.build();
	}

	public void forEach(BiConsumer<MDSite, MDElement> action) {
		MDSite lhs = latticeMd.getLhs();
		rhs.forEach(element -> action.accept(lhs, element));
	}

	public Collection<Rhs> getRhs() {
		double[] lowerBounds = getLowerBounds();
		return getRhs(lowerBounds);
	}

	private double[] getLowerBounds() {
		int[] rhsAttrs = getRhsAttrs();
		return latticeMd.getMaxGenThresholds(rhsAttrs);
	}

	private Collection<Rhs> getRhs(double[] lowerBounds) {
		Collection<Rhs> result = new ArrayList<>(rhs.size());
		int i = 0;
		for (MDElement element : rhs) {
			double lowerBound = lowerBounds[i++];
			Rhs rhs = toRhs(element, lowerBound);
			result.add(rhs);
		}
		return result;
	}

	private int[] getRhsAttrs() {
		return rhs.stream()
			.mapToInt(MDElement::getId)
			.toArray();
	}
}
