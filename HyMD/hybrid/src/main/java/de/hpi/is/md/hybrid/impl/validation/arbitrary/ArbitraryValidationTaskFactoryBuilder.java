package de.hpi.is.md.hybrid.impl.validation.arbitrary;

import static de.hpi.is.md.util.CollectionUtils.sizeBelow;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.impl.validation.RhsValidationTask;
import de.hpi.is.md.hybrid.impl.validation.RhsValidationTaskImpl;
import de.hpi.is.md.hybrid.impl.validation.TrivialRhsValidationTaskFactory;
import de.hpi.is.md.util.IntArrayPair;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Setter
public class ArbitraryValidationTaskFactoryBuilder {

	@NonNull
	private List<PreprocessedColumnPair> columnPairs;
	@NonNull
	private DictionaryRecords leftRecords;
	@NonNull
	private DictionaryRecords rightRecords;
	private double minThreshold;
	@NonNull
	private Predicate<Collection<IntArrayPair>> shouldUpdate = sizeBelow(20);
	private long minSupport;

	public ArbitraryValidationTaskFactory build() {
		LhsValidationTaskFactory factory = createFactory();
		RhsValidationTask.Factory rhsFactory = createRhsFactory();
		RhsValidationTask.Factory trivialFactory = createTrivialFactory();
		return new ArbitraryValidationTaskFactory(columnPairs, factory, rhsFactory, trivialFactory);
	}

	private LhsValidationTaskFactory createFactory() {
		return LhsValidationTaskFactory.builder()
			.leftRecords(leftRecords)
			.rightRecords(rightRecords)
			.minSupport(minSupport)
			.build();
	}

	private RhsValidationTask.Factory createRhsFactory() {
		return RhsValidationTaskImpl.factoryBuilder()
			.rightRecords(rightRecords)
			.minThreshold(minThreshold)
			.shouldUpdate(shouldUpdate)
			.build();
	}

	private RhsValidationTask.Factory createTrivialFactory() {
		return new TrivialRhsValidationTaskFactory(minThreshold);
	}

}
