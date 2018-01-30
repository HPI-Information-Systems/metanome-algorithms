package de.hpi.is.md.hybrid.impl.level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hpi.is.md.hybrid.Analyzer;
import de.hpi.is.md.hybrid.ValidationResult;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.hybrid.impl.infer.ThresholdLowerer;
import de.hpi.is.md.util.IntArrayPair;
import java.util.Collection;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class CandidateProcessorTest {

	@Mock
	private BatchValidator validator;
	@Mock
	private Analyzer analyzer;

	@Test
	public void test() {
		CandidateProcessor processor = createProcessor();
		Collection<Candidate> candidates = Collections.singletonList(Mockito.mock(Candidate.class));
		AnalyzeTask task = Mockito.mock(AnalyzeTask.class);
		Collection<AnalyzeTask> results = Collections
			.singletonList(task);
		when(task.getResult()).thenReturn(Mockito.mock(ValidationResult.class));
		when(validator.validate(candidates)).thenReturn(results);
		when(analyzer.analyze(results)).thenReturn(Mockito.mock(Statistics.class));
		processor.validateAndAnalyze(candidates);
		verify(validator).validate(candidates);
		verify(analyzer).analyze(results);
	}

	@Test
	public void testGetRecommendations() {
		CandidateProcessor processor = createProcessor();
		Collection<Candidate> candidates = Collections.singletonList(Mockito.mock(Candidate.class));
		ValidationResult validationResult = Mockito.mock(ValidationResult.class);
		Collection<AnalyzeTask> results = Collections
			.singletonList(new AnalyzeTask(validationResult, Mockito.mock(ThresholdLowerer.class)));
		RhsResult rhsResult = Mockito.mock(RhsResult.class);
		when(validationResult.getRhsResults()).thenReturn(Collections.singletonList(rhsResult));
		IntArrayPair violation = Mockito.mock(IntArrayPair.class);
		when(rhsResult.getViolations()).thenReturn(Collections.singletonList(violation));
		when(validator.validate(candidates)).thenReturn(results);
		when(analyzer.analyze(results)).thenReturn(Mockito.mock(Statistics.class));
		processor.validateAndAnalyze(candidates);
		Collection<IntArrayPair> recommendations = processor.getRecommendations();
		assertThat(recommendations).hasSize(1);
		assertThat(recommendations).contains(violation);
	}

	private CandidateProcessor createProcessor() {
		return new CandidateProcessor(analyzer, validator);
	}

}