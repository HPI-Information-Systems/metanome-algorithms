package de.hpi.is.md.hybrid.impl.validation.single;

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
public class SingleValidationTaskFactoryBuilder {

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

	public SingleValidationTaskFactory build() {
		RhsValidationTask.Factory rhsFactory = createRhsFactory();
		RhsValidationTask.Factory trivialFactory = createTrivialFactory();
		return new SingleValidationTaskFactory(leftRecords, columnPairs, minSupport, rhsFactory,
			trivialFactory);
	}

	private RhsValidationTask.Factory createTrivialFactory() {
		return new TrivialRhsValidationTaskFactory(minThreshold);
	}

	private RhsValidationTask.Factory createRhsFactory() {
		return RhsValidationTaskImpl.factoryBuilder()
			.minThreshold(minThreshold)
			.rightRecords(rightRecords)
			.shouldUpdate(shouldUpdate)
			.build();
	}

}
