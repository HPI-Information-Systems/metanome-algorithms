package de.hpi.is.md.hybrid.impl.level.analyze;

import de.hpi.is.md.hybrid.Analyzer;
import de.hpi.is.md.hybrid.SupportedMD;
import de.hpi.is.md.hybrid.ValidationResult;
import de.hpi.is.md.hybrid.ValidationResult.LhsResult;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.hybrid.impl.lattice.FullLattice;
import de.hpi.is.md.hybrid.impl.level.AnalyzeTask;
import de.hpi.is.md.hybrid.impl.level.Statistics;
import de.hpi.is.md.hybrid.impl.level.analyze.AnalyzeStrategy.Factory;
import java.util.Collection;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AnalyzerImpl implements Analyzer {

	@NonNull
	private final Factory factory;

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public Statistics analyze(Iterable<AnalyzeTask> results) {
		return withStatistics().analyze(results);
	}

	private WithStatistics withStatistics() {
		return new WithStatistics();
	}

	@Accessors(fluent = true)
	@Setter
	public static class Builder {

		private long minSupport;
		@NonNull
		private Consumer<SupportedMD> consumer;
		@NonNull
		private InferHandler inferHandler;
		@NonNull
		private FullLattice fullLattice;

		public Analyzer build() {
			Factory supportedFactory = SupportedStrategy.factory(consumer, inferHandler);
			Factory notSupportedFactory = NotSupportedStrategy.factory(fullLattice);
			Factory factory = SupportBasedFactory.builder()
				.supportedFactory(supportedFactory)
				.notSupportedFactory(notSupportedFactory)
				.minSupport(minSupport)
				.build();
			return new AnalyzerImpl(factory);
		}

	}

	private class WithStatistics {

		private final Statistics statistics = new Statistics();

		private Statistics analyze(Iterable<AnalyzeTask> results) {
			results.forEach(this::deduce);
			return statistics;
		}

		private void deduce(AnalyzeTask analyzeTask) {
			analyzeTask.lower();
			ValidationResult result = analyzeTask.getResult();
			deduce(result);
		}

		private void deduce(ValidationResult result) {
			LhsResult lhsResult = result.getLhsResult();
			Collection<RhsResult> rhsResults = result.getRhsResults();
			AnalyzeStrategy strategy = factory.create(lhsResult);
			statistics.groupedValidation();
			rhsResults.forEach(strategy::deduce);
			Statistics newStatistics = strategy.getStatistics();
			statistics.add(newStatistics);
		}

	}
}
