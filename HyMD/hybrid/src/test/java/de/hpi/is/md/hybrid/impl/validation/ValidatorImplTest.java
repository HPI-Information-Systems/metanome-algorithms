package de.hpi.is.md.hybrid.impl.validation;

import static de.hpi.is.md.util.CollectionUtils.sizeBelow;

import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.Validator;
import de.hpi.is.md.hybrid.ValidatorTest;
import java.util.List;

public class ValidatorImplTest extends ValidatorTest {

	@Override
	protected Validator createValidator(List<PreprocessedColumnPair> columnPairs,
		DictionaryRecords left, DictionaryRecords right, double minThreshold) {
		return ValidatorImpl.builder()
			.columnPairs(columnPairs)
			.leftRecords(left)
			.rightRecords(right)
			.minThreshold(minThreshold)
			.shouldUpdate(sizeBelow(5))
			.minSupport(0)
			.build();
	}

}