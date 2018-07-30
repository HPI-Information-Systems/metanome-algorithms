package de.hpi.is.md.hybrid.impl.validation;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.Validator;
import de.hpi.is.md.hybrid.impl.validation.arbitrary.ArbitraryValidationTaskFactory;
import de.hpi.is.md.hybrid.impl.validation.empty.EmptyValidationTaskFactory;
import de.hpi.is.md.hybrid.impl.validation.single.SingleValidationTaskFactory;
import de.hpi.is.md.util.IntArrayPair;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Setter
public class ValidatorBuilder {

	@NonNull
	private List<PreprocessedColumnPair> columnPairs;
	@NonNull
	private DictionaryRecords leftRecords;
	@NonNull
	private DictionaryRecords rightRecords;
	private double minThreshold;
	@NonNull
	private Predicate<Collection<IntArrayPair>> shouldUpdate = Collection::isEmpty;
	private long minSupport;

	public Validator build() {
		ArbitraryValidationTaskFactory arbitrary = ArbitraryValidationTaskFactory.builder()
			.columnPairs(columnPairs)
			.leftRecords(leftRecords)
			.minSupport(minSupport)
			.rightRecords(rightRecords)
			.shouldUpdate(shouldUpdate)
			.minThreshold(minThreshold)
			.leftRecords(leftRecords)
			.build();
		EmptyValidationTaskFactory empty = EmptyValidationTaskFactory.builder()
			.columnPairs(columnPairs)
			.leftRecords(leftRecords)
			.rightRecords(rightRecords)
			.minThreshold(minThreshold)
			.build();
		SingleValidationTaskFactory single = SingleValidationTaskFactory.builder()
			.columnPairs(columnPairs)
			.leftRecords(leftRecords)
			.rightRecords(rightRecords)
			.minThreshold(minThreshold)
			.minSupport(minSupport)
			.shouldUpdate(shouldUpdate)
			.leftRecords(leftRecords)
			.build();
		return new ValidatorImpl(arbitrary, empty, single);
	}

}
