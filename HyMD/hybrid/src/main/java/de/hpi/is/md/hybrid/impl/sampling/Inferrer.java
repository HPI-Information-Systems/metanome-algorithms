package de.hpi.is.md.hybrid.impl.sampling;

import de.hpi.is.md.hybrid.SimilaritySet;
import de.hpi.is.md.hybrid.impl.infer.ThresholdLowerer;
import de.hpi.is.md.hybrid.impl.lattice.FullLattice;
import de.hpi.is.md.hybrid.impl.md.MDImpl;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Collection;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
class Inferrer {

	@Getter
	private final Statistics statistics = new Statistics();
	@NonNull
	private final MDSite lhs;
	@NonNull
	private final ThresholdLowerer lowerer;
	@NonNull
	private final SimilaritySet similaritySet;
	@NonNull
	private final MDSpecializer specializer;
	@NonNull
	private final FullLattice fullLattice;

	void infer(MDElement rhs) {
		logCurrent(rhs);
		int rhsAttr = rhs.getId();
		lowerThreshold(rhsAttr);
		specialize(rhs);
	}

	private void addIfMinimal(MD md) {
		fullLattice.addIfMinimalAndSupported(md)
			.ifPresent(__ -> newDeduced(md));
	}

	private void logCurrent(MDElement rhs) {
		MD md = new MDImpl(lhs, rhs);
		log.trace("{} violated by {}", md, similaritySet);
	}

	private void lowerThreshold(int rhsAttr) {
		double similarity = similaritySet.get(rhsAttr);
		lowerer.lowerThreshold(rhsAttr, similarity);
	}

	private void newDeduced(MD md) {
		statistics.newDeduced();
		log.trace("added {}", md);
	}

	private void specialize(MDElement rhs) {
		Collection<MD> specialized = specializer.specialize(lhs, rhs, similaritySet);
		specialized.forEach(this::addIfMinimal);
	}
}
