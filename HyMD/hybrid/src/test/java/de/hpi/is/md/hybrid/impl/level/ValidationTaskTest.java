package de.hpi.is.md.hybrid.impl.level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.hpi.is.md.hybrid.Rhs;
import de.hpi.is.md.hybrid.ValidationResult;
import de.hpi.is.md.hybrid.ValidationResult.LhsResult;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.hybrid.Validator;
import de.hpi.is.md.hybrid.impl.infer.ThresholdLowerer;
import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ValidationTaskTest {

	@Mock
	private Validator validator;
	@Mock
	private ThresholdLowerer lowerer;

	@Test
	public void test() {
		int columnPairs = 4;
		Collection<Rhs> rhs = Arrays
			.asList(Rhs.builder().rhsAttr(0).threshold(1.0).lowerBound(0.0).build(),
				Rhs.builder().rhsAttr(1).threshold(1.0).lowerBound(0.0).build());
		MDSite lhs = new MDSiteImpl(columnPairs);
		RhsResult expected1 = RhsResult.builder()
			.rhsAttr(0)
			.threshold(0.5)
			.violations(Collections.emptyList())
			.validAndMinimal(false)
			.build();
		RhsResult expected2 = RhsResult.builder()
			.rhsAttr(1)
			.threshold(0.6)
			.violations(Collections.emptyList())
			.validAndMinimal(true)
			.build();
		when(validator.validate(lhs, rhs))
			.thenReturn(new ValidationResult(Mockito.mock(LhsResult.class),
				Arrays.asList(expected1, expected2)));
		ValidationTask task = createTask(lhs, rhs);
		AnalyzeTask analyzeTask = task.validate();
		ValidationResult result = analyzeTask.getResult();
		assertThat(result.getRhsResults()).contains(expected1);
		assertThat(result.getRhsResults()).contains(expected2);
	}

	private ValidationTask createTask(MDSite lhs, Collection<Rhs> rhs) {
		return ValidationTask.builder()
			.lhs(lhs)
			.lowerer(lowerer)
			.rhs(rhs)
			.validator(validator)
			.build();
	}

}