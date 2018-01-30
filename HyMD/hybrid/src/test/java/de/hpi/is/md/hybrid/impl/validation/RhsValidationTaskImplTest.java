package de.hpi.is.md.hybrid.impl.validation;

import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.hybrid.Rhs;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class RhsValidationTaskImplTest extends RhsValidationTaskTest {

	@Override
	protected RhsValidationTask create(Rhs rhs, PreprocessedColumnPair columnPair) {
		RhsValidationTask.Factory factory = RhsValidationTaskImpl.factoryBuilder()
			.minThreshold(minThreshold)
			.rightRecords(rightRecords)
			.shouldUpdate(shouldUpdate)
			.build();
		return factory.create(rhs, columnPair, 0.0);
	}

}