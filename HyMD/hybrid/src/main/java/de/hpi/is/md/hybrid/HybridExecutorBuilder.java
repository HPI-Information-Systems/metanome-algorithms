package de.hpi.is.md.hybrid;

import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.MatchingDependencyResult;
import de.hpi.is.md.ThresholdFilter;
import de.hpi.is.md.ThresholdProvider;
import de.hpi.is.md.hybrid.impl.infer.SpecializationFilter;
import de.hpi.is.md.hybrid.impl.lattice.FullLattice;
import de.hpi.is.md.hybrid.impl.lattice.lhs.LhsLattice;
import de.hpi.is.md.hybrid.impl.lattice.md.LevelFunction;
import de.hpi.is.md.hybrid.impl.level.LevelStrategy;
import de.hpi.is.md.hybrid.impl.level.LevelWiseExecutor;
import de.hpi.is.md.hybrid.impl.sampling.SamplingExecutor;
import de.hpi.is.md.impl.threshold.MultiThresholdProvider;
import de.hpi.is.md.util.BetterConsumer;
import de.hpi.is.md.util.MathUtils;
import de.hpi.is.md.util.MetricsUtils;
import de.hpi.is.md.util.StreamUtils;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Seq;

@Slf4j
@Accessors(fluent = true)
@Setter
class HybridExecutorBuilder {

	@NonNull
	private final DictionaryRecords leftRecords;
	@NonNull
	private final DictionaryRecords rightRecords;
	@NonNull
	private final List<PreprocessedColumnPair> columnPairs;
	@NonNull
	private final List<ColumnMapping<?>> mappings;
	@NonNull
	private List<ThresholdFilter> thresholdFilters;
	private double minThreshold;
	@NonNull
	private BetterConsumer<MatchingDependencyResult> consumer;
	private boolean parallel;
	@NonNull
	private SpecializationFilter specializationFilter;
	@NonNull
	private LevelBundle levelBundle = LevelBundle.CARDINALITY;
	private long minSupport;

	HybridExecutorBuilder(Preprocessed preprocessed) {
		this.leftRecords = preprocessed.getLeftRecords();
		this.rightRecords = preprocessed.getRightRecords();
		this.columnPairs = preprocessed.getColumnPairs();
		this.mappings = preprocessed.getMappings();
	}

	private static Iterable<Double> getThresholds(ThresholdFilter thresholdFilter,
		PreprocessedColumnPair preprocessedColumnPair) {
		return preprocessedColumnPair.getThresholds(thresholdFilter);
	}

	HybridExecutor build() {
		int size = columnPairs.size();
		ThresholdProvider thresholdProvider = createThresholdProvider();
		List<DoubleSortedSet> thresholds = thresholdProvider.getAll();
		logStatistics(thresholds);
		LevelFunction levelFunction = levelBundle.createLevelFunction(thresholds);
		Lattice lattice = LatticeHelper.createLattice(levelFunction);
		LhsLattice notSupported = new LhsLattice(size);
		FullLattice fullLattice = new FullLattice(lattice, notSupported);
		LevelWiseExecutor levelWiseExecutor = buildLevelWiseExecutor(fullLattice,
			thresholdProvider);
		SamplingExecutor samplingExecutor = buildSamplingExecutor(fullLattice, thresholdProvider);
		return new HybridExecutor(levelWiseExecutor, samplingExecutor);
	}

	private Consumer<SupportedMD> buildConsumer() {
		ResultTransformer transformer = new ResultTransformer(mappings);
		return consumer.compose(transformer::transform);
	}

	private LevelWiseExecutor buildLevelWiseExecutor(FullLattice fullLattice,
		ThresholdProvider thresholdProvider) {
		Consumer<SupportedMD> consumer = buildConsumer();
		LevelStrategy levelStrategy = levelBundle.createLevelStrategy(fullLattice, minThreshold);
		return LevelWiseExecutor.builder()
			.columnPairs(columnPairs)
			.minSupport(minSupport)
			.minThreshold(minThreshold)
			.consumer(consumer)
			.fullLattice(fullLattice)
			.leftRecords(leftRecords)
			.rightRecords(rightRecords)
			.parallel(parallel)
			.thresholdProvider(thresholdProvider)
			.specializationFilter(specializationFilter)
			.levelStrategy(levelStrategy)
			.build();
	}

	private SamplingExecutor buildSamplingExecutor(FullLattice fullLattice,
		ThresholdProvider thresholdProvider) {
		return SamplingExecutor.builder()
			.columnPairs(columnPairs)
			.thresholdProvider(thresholdProvider)
			.fullLattice(fullLattice)
			.leftRecords(leftRecords)
			.rightRecords(rightRecords)
			.parallel(parallel)
			.specializationFilter(specializationFilter)
			.build();
	}

	private ThresholdProvider createThresholdProvider() {
		List<Iterable<Double>> similarities = Seq.zip(thresholdFilters, columnPairs)
			.map(t -> t.map(HybridExecutorBuilder::getThresholds))
			.toList();
		return MultiThresholdProvider.create(similarities);
	}

	private void logStatistics(Iterable<DoubleSortedSet> thresholds) {
		int[] numThresholds = StreamUtils.seq(thresholds)
			.mapToInt(Collection::size)
			.toArray();
		log.info("Using {} thresholds", numThresholds);
		long numLhs = StreamUtils.seq(thresholds)
			.mapToLong(Collection::size)
			.map(MathUtils::increment)
			.reduce(1, MathUtils::multiply);
		MetricsUtils.registerGauge("possibleLHS", numLhs);
		MetricsUtils.registerGauge("minSupport", minSupport);
		MetricsUtils.registerGauge("thresholds", thresholds);
		log.info("{} possible LHSs", numLhs);
		log.info("minSupport: {}", minSupport);
		log.info("minThreshold: {}", minThreshold);
		log.info("thresholds: {}", thresholds);
	}

}
