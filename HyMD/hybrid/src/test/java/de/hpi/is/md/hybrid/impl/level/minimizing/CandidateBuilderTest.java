package de.hpi.is.md.hybrid.impl.level.minimizing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.Rhs;
import de.hpi.is.md.hybrid.impl.level.Candidate;
import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Collection;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class CandidateBuilderTest {

	@Mock
	private LatticeMD latticeMD;

	@Test
	public void test() {
		CandidateBuilder builder = new CandidateBuilder(0.0, new SimpleMinimizer());
		when(latticeMD.getRhs()).thenReturn(new MDSiteImpl(4).set(0, 0.6).set(1, 0.7));
		when(latticeMD.getLhs()).thenReturn(Mockito.mock(MDSite.class));
		when(latticeMD.getMaxGenThresholds(new int[]{0, 1})).thenReturn(new double[]{0.0, 0.5});
		Collection<Candidate> candidates = builder
			.toCandidates(Collections.singletonList(latticeMD));
		assertThat(candidates).hasSize(1);
		Candidate candidate = Iterables.get(candidates, 0);
		assertThat(candidate.getRhs()).hasSize(2);
		assertThat(candidate.getRhs())
			.contains(Rhs.builder().rhsAttr(0).threshold(0.6).lowerBound(0.0).build());
		assertThat(candidate.getRhs())
			.contains(Rhs.builder().rhsAttr(1).threshold(0.7).lowerBound(0.5).build());
		assertThat(candidate.getLatticeMd()).isEqualTo(latticeMD);
	}

	@Test
	public void testEmpty() {
		CandidateBuilder builder = new CandidateBuilder(0.0, new SimpleMinimizer());
		when(latticeMD.getRhs()).thenReturn(new MDSiteImpl(4));
		Collection<Candidate> candidates = builder
			.toCandidates(Collections.singletonList(latticeMD));
		assertThat(candidates).isEmpty();
	}

	@Test
	public void testInvalid() {
		CandidateBuilder builder = new CandidateBuilder(0.7, new SimpleMinimizer());
		int columnPairs = 4;
		when(latticeMD.getLhs()).thenReturn(new MDSiteImpl(columnPairs));
		when(latticeMD.getRhs()).thenReturn(new MDSiteImpl(columnPairs).set(0, 0.6).set(1, 0.7));
		when(latticeMD.getMaxGenThresholds(new int[]{1})).thenReturn(new double[]{0.5});
		Collection<Candidate> candidates = builder
			.toCandidates(Collections.singletonList(latticeMD));
		Candidate candidate = Iterables.get(candidates, 0);
		Collection<Rhs> rhs = Lists.newArrayList(candidate.getRhs());
		assertThat(rhs).hasSize(1);
		assertThat(rhs).contains(Rhs.builder().rhsAttr(1).threshold(0.7).lowerBound(0.5).build());
		assertThat(candidate.getLatticeMd()).isEqualTo(latticeMD);
	}

	@Test
	public void testValidatedBefore() {
		CandidateBuilder builder = new CandidateBuilder(0.0, new SimpleMinimizer());
		when(latticeMD.getRhs()).thenReturn(new MDSiteImpl(4).set(0, 0.6));
		when(latticeMD.getLhs()).thenReturn(Mockito.mock(MDSite.class));
		builder.toCandidates(Collections.singletonList(latticeMD));
		Collection<Candidate> candidates = builder
			.toCandidates(Collections.singletonList(latticeMD));
		assertThat(candidates).isEmpty();
	}

}