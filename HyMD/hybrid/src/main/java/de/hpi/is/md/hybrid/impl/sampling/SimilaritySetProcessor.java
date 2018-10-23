package de.hpi.is.md.hybrid.impl.sampling;

import com.codahale.metrics.annotation.Timed;
import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.SimilaritySet;
import de.hpi.is.md.hybrid.impl.infer.ThresholdLowerer;
import de.hpi.is.md.hybrid.impl.lattice.FullLattice;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import java.util.HashSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Metrics
@RequiredArgsConstructor
class SimilaritySetProcessor {

	@NonNull
	private final FullLattice fullLattice;
	@NonNull
	private final MDSpecializer specializer;
	private final Collection<SimilaritySet> seen = new HashSet<>();
	@Setter
	private boolean processAll = false;

	@Timed
	Statistics process(SimilaritySet similaritySet) {
		return with(similaritySet).process();
	}

	private WithSimilaritySet with(SimilaritySet similaritySet) {
		return new WithSimilaritySet(similaritySet);
	}

	@RequiredArgsConstructor
	private class WithSimilaritySet {

		private final Statistics statistics = new Statistics();
		@NonNull
		private final SimilaritySet similaritySet;

		private Inferrer createTask(LatticeMD latticeMD) {
			MDSite lhs = latticeMD.getLhs();
			ThresholdLowerer lowerer = new ThresholdLowerer(latticeMD);
			return createTask(lhs, lowerer);
		}

		private Inferrer createTask(MDSite lhs, ThresholdLowerer lowerer) {
			return Inferrer.builder()
				.specializer(specializer)
				.fullLattice(fullLattice)
				.lowerer(lowerer)
				.lhs(lhs)
				.similaritySet(similaritySet)
				.build();
		}

		private Collection<LatticeMD> findViolated() {
			return fullLattice.findViolated(similaritySet);
		}

		private Statistics process(LatticeMD latticeMD) {
			Inferrer task = createTask(latticeMD);
			Iterable<MDElement> rhs = latticeMD.getRhs();
			StreamUtils.seq(rhs)
				.filter(similaritySet::isViolated)
				.forEach(task::infer);
			return task.getStatistics();
		}

		private Statistics process() {
			if (processAll || unseenBefore()) {
				processNotSeen();
			}
			return statistics;
		}

		private void process(Collection<LatticeMD> violated) {
			violated.stream()
				.map(this::process)
				.forEach(statistics::add);
		}

		private void processNotSeen() {
			Collection<LatticeMD> violated = findViolated();
			log.trace("Found {} violated LatticeMDs", Integer.valueOf(violated.size()));
			process(violated);
			statistics.processed();
			assert findViolated().isEmpty() : "Violated MDs remain in lattice";
		}

		private boolean unseenBefore() {
			return seen.add(similaritySet);
		}
	}

}
