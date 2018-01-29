package de.hpi.is.md.hybrid.impl.level.analyze;

import de.hpi.is.md.hybrid.impl.lattice.FullLattice;
import de.hpi.is.md.hybrid.impl.level.Statistics;
import de.hpi.is.md.hybrid.md.MD;
import java.util.Collection;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
public class InferHandler {

	@NonNull
	private final MDSpecializer specializer;
	@NonNull
	private final FullLattice fullLattice;

	Statistics infer(MD md) {
		log.trace("Inferring from {}", md);
		Collection<MD> result = specializer.specialize(md);
		return withStatistics().addAll(result);
	}

	private WithStatistics withStatistics() {
		return new WithStatistics();
	}

	private class WithStatistics {

		private final Statistics statistics = new Statistics();

		private Statistics addAll(Iterable<MD> result) {
			result.forEach(this::addIfMinimal);
			return statistics;
		}

		private void addIfMinimal(MD md) {
			fullLattice.addIfMinimalAndSupported(md)
				.ifPresent(__ -> newDeduced(md));
		}

		private void newDeduced(MD md) {
			statistics.newDeduced();
			log.trace("Deduced {}", md);
		}
	}
}
