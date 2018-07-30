package de.hpi.is.md.hybrid.impl.level;

import static de.hpi.is.md.util.CollectionUtils.sizeBelow;

import de.hpi.is.md.ThresholdProvider;
import de.hpi.is.md.hybrid.Analyzer;
import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.SupportedMD;
import de.hpi.is.md.hybrid.Validator;
import de.hpi.is.md.hybrid.impl.infer.FullLhsSpecializer;
import de.hpi.is.md.hybrid.impl.infer.FullSpecializer;
import de.hpi.is.md.hybrid.impl.infer.LhsSpecializer;
import de.hpi.is.md.hybrid.impl.infer.SpecializationFilter;
import de.hpi.is.md.hybrid.impl.lattice.FullLattice;
import de.hpi.is.md.hybrid.impl.level.analyze.AnalyzerImpl;
import de.hpi.is.md.hybrid.impl.level.analyze.InferHandler;
import de.hpi.is.md.hybrid.impl.level.analyze.MDSpecializer;
import de.hpi.is.md.hybrid.impl.validation.ValidatorImpl;
import de.hpi.is.md.util.IntArrayPair;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Setter
public class LevelWiseExecutorBuilder {

	@NonNull
	private Consumer<SupportedMD> consumer;
	@NonNull
	private List<PreprocessedColumnPair> columnPairs;
	@NonNull
	private DictionaryRecords leftRecords;
	@NonNull
	private DictionaryRecords rightRecords;
	@NonNull
	private Predicate<Statistics> evaluator = statistics -> statistics.getRounds() > 0;
	private boolean parallel;
	@NonNull
	private ThresholdProvider thresholdProvider;
	private double minThreshold;
	private long minSupport;
	@NonNull
	private FullLattice fullLattice;
	@NonNull
	private SpecializationFilter specializationFilter;
	@NonNull
	private LevelStrategy levelStrategy;
	@NonNull
	private Predicate<Collection<IntArrayPair>> shouldUpdate = sizeBelow(5);

	public LevelWiseExecutor build() {
		CandidateProcessor processor = buildProcessor();
		return new LevelWiseExecutor(levelStrategy, evaluator, processor);
	}

	private Analyzer buildAnalyzer() {
		InferHandler inferHandler = buildInferHandler();
		return AnalyzerImpl.builder()
			.minSupport(minSupport)
			.inferHandler(inferHandler)
			.consumer(consumer)
			.fullLattice(fullLattice)
			.build();
	}

	private BatchValidator buildBatchValidator() {
		Validator validator = buildValidator();
		return new BatchValidator(validator, parallel);
	}

	private InferHandler buildInferHandler() {
		MDSpecializer specializer = buildSpecializer();
		return InferHandler.builder()
			.specializer(specializer)
			.fullLattice(fullLattice)
			.build();
	}

	private CandidateProcessor buildProcessor() {
		BatchValidator validation = buildBatchValidator();
		Analyzer analyzer = buildAnalyzer();
		return new CandidateProcessor(analyzer, validation);
	}

	private MDSpecializer buildSpecializer() {
		LhsSpecializer lhsSpecializer = new LhsSpecializer(thresholdProvider);
		FullLhsSpecializer fullLhsSpecializer = new FullLhsSpecializer(lhsSpecializer);
		FullSpecializer specializer = new FullSpecializer(fullLhsSpecializer, specializationFilter);
		return new MDSpecializer(specializer);
	}

	private Validator buildValidator() {
		return ValidatorImpl.builder()
			.shouldUpdate(shouldUpdate)
			.columnPairs(columnPairs)
			.leftRecords(leftRecords)
			.rightRecords(rightRecords)
			.minThreshold(minThreshold)
			.minSupport(minSupport)
			.build();
	}

}
