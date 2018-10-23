package de.hpi.is.md.hybrid;

import com.codahale.metrics.annotation.Timed;
import de.hpi.is.md.Discoverer;
import de.hpi.is.md.MatchingDependencyResult;
import de.hpi.is.md.SupportCalculator;
import de.hpi.is.md.ThresholdFilter;
import de.hpi.is.md.hybrid.impl.infer.SpecializationFilter;
import de.hpi.is.md.hybrid.impl.preprocessed.PreprocessorImpl;
import de.hpi.is.md.relational.Relation;
import de.hpi.is.md.result.AbstractResultEmitter;
import de.hpi.is.md.util.DiskCache;
import io.astefanutti.metrics.aspectj.Metrics;
import java.io.File;
import java.util.List;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Builder
@Metrics
public class HybridDiscoverer extends AbstractResultEmitter<MatchingDependencyResult> implements
	Discoverer {

	@NonNull
	private final DiscoveryConfiguration configuration;
	@NonNull
	private final MDMapping mappings;
	@Default
	private boolean parallel = false;
	@Default
	private boolean store = true;
	@Default
	@NonNull
	private File cacheDirectory = new File("preprocessed/");

	@Override
	@Timed
	public void discover(Relation r, Relation s) {
		HybridExecutor executor = with(r, s).createExecutor();
		executor.discover();
	}

	private HybridExecutor create(Preprocessed preprocessed, long minSupport) {
		double minThreshold = configuration.getMinThreshold();
		SpecializationFilter specializationFilter = configuration.getSpecializationFilter();
		List<ThresholdFilter> thresholdFilters = mappings.getThresholdFilters();
		LevelBundle levelBundle = configuration.getLevelBundle();
		return HybridExecutor.builder(preprocessed)
			.minSupport(minSupport)
			.minThreshold(minThreshold)
			.thresholdFilters(thresholdFilters)
			.parallel(parallel)
			.consumer(this::emitResult)
			.specializationFilter(specializationFilter)
			.levelBundle(levelBundle)
			.build();
	}

	private WithRelations with(Relation r, Relation s) {
		return new WithRelations(r, s);
	}

	@RequiredArgsConstructor
	private class WithRelations {

		private final Relation r;
		private final Relation s;

		private long calculateMinSupport() {
			SupportCalculator supportCalculator = configuration.getSupportCalculator();
			return supportCalculator.calculateSupport(r, s);
		}

		private HybridExecutor createExecutor() {
			Preprocessed preprocessed = preprocess();
			long minSupport = calculateMinSupport();
			return create(preprocessed, minSupport);
		}

		private Preprocessor createPreprocessor() {
			PreprocessingConfiguration preprocessingConfiguration = mappings
				.getPreprocessingConfiguration();
			return PreprocessorImpl.builder()
				.mappings(preprocessingConfiguration)
				.left(r)
				.right(s)
				.build();
		}

		private Preprocessed preprocess() {
			Preprocessor preprocessor = createPreprocessor();
			DiskCache<Preprocessed> cache = new DiskCache<>(preprocessor, cacheDirectory);
			return cache.get(store);
		}
	}

}
