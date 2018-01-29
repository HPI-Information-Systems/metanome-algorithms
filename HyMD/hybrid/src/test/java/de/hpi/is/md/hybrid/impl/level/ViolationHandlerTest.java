package de.hpi.is.md.hybrid.impl.level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.hpi.is.md.hybrid.ValidationResult;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.util.IntArrayPair;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ViolationHandlerTest {

	@Test
	public void test() {
		ViolationHandler violationHandler = new ViolationHandler();
		AnalyzeTask task = Mockito.mock(AnalyzeTask.class);
		ValidationResult validationResult = Mockito.mock(ValidationResult.class);
		when(task.getResult()).thenReturn(validationResult);
		RhsResult rhsResult1 = Mockito.mock(RhsResult.class);
		RhsResult rhsResult2 = Mockito.mock(RhsResult.class);
		when(rhsResult1.getViolations()).thenReturn(Arrays
			.asList(new IntArrayPair(new int[]{1}, new int[]{1}),
				new IntArrayPair(new int[]{2}, new int[]{2})));
		when(rhsResult2.getViolations()).thenReturn(Arrays
			.asList(new IntArrayPair(new int[]{2}, new int[]{2}),
				new IntArrayPair(new int[]{3}, new int[]{3})));
		when(validationResult.getRhsResults()).thenReturn(Arrays.asList(rhsResult1, rhsResult2));
		violationHandler.addViolations(task);
		Collection<IntArrayPair> violations = violationHandler.pollViolations();
		assertThat(violations).hasSize(3);
		assertThat(violations).contains(new IntArrayPair(new int[]{1}, new int[]{1}));
		assertThat(violations).contains(new IntArrayPair(new int[]{2}, new int[]{2}));
		assertThat(violations).contains(new IntArrayPair(new int[]{3}, new int[]{3}));
		assertThat(violationHandler.pollViolations()).isEmpty();
	}

	@Test
	public void testEmpty() {
		ViolationHandler violationHandler = new ViolationHandler();
		assertThat(violationHandler.pollViolations()).isEmpty();
	}

}