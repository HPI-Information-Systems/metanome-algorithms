package de.hpi.is.md.hybrid.impl.sampling;

import de.hpi.is.md.ThresholdProvider;
import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.Sampler;
import de.hpi.is.md.hybrid.impl.infer.FullLhsSpecializer;
import de.hpi.is.md.hybrid.impl.infer.FullSpecializer;
import de.hpi.is.md.hybrid.impl.infer.LhsSpecializer;
import de.hpi.is.md.hybrid.impl.infer.SpecializationFilter;
import de.hpi.is.md.hybrid.impl.lattice.FullLattice;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Setter
public class SamplingExecutorBuilder {

	@NonNull
	private List<PreprocessedColumnPair> columnPairs;
	@NonNull
	private Iterable<int[]> leftRecords;
	@NonNull
	private DictionaryRecords rightRecords;
	@NonNull
	private ThresholdProvider thresholdProvider;
	@NonNull
	private FullLattice fullLattice;
	@NonNull
	private Predicate<Statistics> evaluator = statistics -> statistics.getCount() >= 2
		|| statistics.getProcessed() > 100;
	private boolean parallel;
	@NonNull
	private SpecializationFilter specializationFilter;

	public SamplingExecutor build() {
		Sampler sampler = buildSampler();
		SimilaritySetProcessor processor = buildProcessor();
		return new SamplingExecutor(sampler, processor, evaluator);
	}

	private SimilaritySetProcessor buildProcessor() {
		MDSpecializer specializer = buildSpecializer();
		return new SimilaritySetProcessor(fullLattice, specializer);
	}

	private Sampler buildSampler() {
		Iterator<int[]> left = leftRecords.iterator();
		return SamplerImpl.builder()
			.columnPairs(columnPairs)
			.left(left)
			.right(rightRecords)
			.parallel(parallel)
			.build();
	}

	private MDSpecializer buildSpecializer() {
		LhsSpecializer lhsSpecializer = new LhsSpecializer(thresholdProvider);
		FullLhsSpecializer fullLhsSpecializer = new FullLhsSpecializer(lhsSpecializer);
		FullSpecializer specializer = new FullSpecializer(fullLhsSpecializer, specializationFilter);
		return new MDSpecializer(specializer);
	}
}
