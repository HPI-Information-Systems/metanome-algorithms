package de.hpi.is.md.hybrid.impl.level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.Rhs;
import de.hpi.is.md.hybrid.ValidationResult;
import de.hpi.is.md.hybrid.Validator;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

@RunWith(Parameterized.class)
public class BatchValidatorTest {

	@Rule
	public MockitoRule mockito = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);
	@Parameter
	public boolean parallel;
	@Mock
	private Validator validator;

	@Parameters
	public static Collection<Boolean> data() {
		return Arrays.asList(true, false);
	}

	@Test
	public void test() {
		BatchValidator batchValidator = new BatchValidator(validator, parallel);
		Candidate candidate = Mockito.mock(Candidate.class);
		LatticeMD latticeMD = Mockito.mock(LatticeMD.class);
		MDSite lhs = Mockito.mock(MDSite.class);
		Collection<Rhs> rhs = Collections.emptyList();
		ValidationResult validationResult = Mockito.mock(ValidationResult.class);
		when(candidate.getLatticeMd()).thenReturn(latticeMD);
		when(candidate.getRhs()).thenReturn(rhs);
		when(latticeMD.getLhs()).thenReturn(lhs);
		when(validator.validate(lhs, rhs)).thenReturn(validationResult);
		Iterable<AnalyzeTask> results = batchValidator
			.validate(Arrays.asList(candidate, candidate));
		assertThat(results).hasSize(2);
		verify(validator, times(2)).validate(lhs, rhs);
	}

}