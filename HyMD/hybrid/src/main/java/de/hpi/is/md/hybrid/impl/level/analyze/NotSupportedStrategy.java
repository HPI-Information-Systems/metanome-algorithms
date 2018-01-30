package de.hpi.is.md.hybrid.impl.level.analyze;

import de.hpi.is.md.hybrid.ValidationResult.LhsResult;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.hybrid.impl.lattice.FullLattice;
import de.hpi.is.md.hybrid.impl.level.Statistics;
import de.hpi.is.md.hybrid.md.MDSite;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
class NotSupportedStrategy implements AnalyzeStrategy {

	@Getter
	private final Statistics statistics = new Statistics();
	private final long support;
	@NonNull
	private final MDSite lhs;
	@NonNull
	private final FullLattice fullLattice;

	public static Factory factory(FullLattice fullLattice) {
		return new FactoryImpl(fullLattice);
	}

	@Override
	public void deduce(RhsResult rhsResult) {
		statistics.validated();
		// if a candidate does not have minimal support, no specialization can be supported
		statistics.notSupported();
		fullLattice.markNotSupported(lhs);
		log.debug("{} not supported (support={})", lhs, support);
	}

	@RequiredArgsConstructor
	private static class FactoryImpl implements Factory {

		private final FullLattice fullLattice;

		@Override
		public AnalyzeStrategy create(LhsResult lhsResult) {
			MDSite lhs = lhsResult.getLhs();
			long support = lhsResult.getSupport();
			return NotSupportedStrategy.builder()
				.support(support)
				.lhs(lhs)
				.fullLattice(fullLattice)
				.build();
		}
	}
}
