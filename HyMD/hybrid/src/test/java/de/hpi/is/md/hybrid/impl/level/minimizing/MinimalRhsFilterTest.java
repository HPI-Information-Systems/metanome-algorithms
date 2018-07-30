package de.hpi.is.md.hybrid.impl.level.minimizing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MinimalRhsFilterTest {

	@Test
	public void testAffected() {
		LatticeMD latticeMd1 = Mockito.mock(LatticeMD.class);
		LatticeMD latticeMd2 = Mockito.mock(LatticeMD.class);
		int columnPairs = 2;
		MDSite lhs1 = new MDSiteImpl(columnPairs).set(0, 0.5);
		when(latticeMd1.getLhs()).thenReturn(lhs1);
		MDSite lhs2 = new MDSiteImpl(columnPairs).set(0, 0.6);
		when(latticeMd2.getLhs()).thenReturn(lhs2);
		List<MDElement> rhs = Collections.singletonList(new MDElementImpl(3, 0.5));
		IntermediateCandidate candidate1 = new IntermediateCandidate(latticeMd1, rhs);
		IntermediateCandidate candidate2 = new IntermediateCandidate(latticeMd2, rhs);
		Collection<IntermediateCandidate> candidates = Arrays.asList(candidate1, candidate2);
		MinimalRhsFilter filter1 = MinimalRhsFilter.create(lhs1, rhs);
		assertThat(filter1.asMinimal(candidates)).hasSize(1);
		assertThat(filter1.asMinimal(candidates)).contains(new MDElementImpl(3, 0.5));
		MinimalRhsFilter filter2 = MinimalRhsFilter.create(lhs2, rhs);
		assertThat(filter2.asMinimal(candidates)).isEmpty();
	}

	@Test
	public void testAffectedMultipleRhs() {
		LatticeMD latticeMd1 = Mockito.mock(LatticeMD.class);
		LatticeMD latticeMd2 = Mockito.mock(LatticeMD.class);
		int columnPairs = 2;
		MDSite lhs1 = new MDSiteImpl(columnPairs).set(0, 0.5);
		when(latticeMd1.getLhs()).thenReturn(lhs1);
		MDSite lhs2 = new MDSiteImpl(columnPairs).set(0, 0.6);
		when(latticeMd2.getLhs()).thenReturn(lhs2);
		Collection<MDElement> rhs1 = Collections.singletonList(new MDElementImpl(3, 0.5));
		IntermediateCandidate candidate1 = new IntermediateCandidate(latticeMd1, rhs1);
		List<MDElement> rhs2 = Arrays.asList(new MDElementImpl(3, 0.5), new MDElementImpl(4, 0.5));
		IntermediateCandidate candidate2 = new IntermediateCandidate(latticeMd2, rhs2);
		Collection<IntermediateCandidate> candidates = Arrays.asList(candidate1, candidate2);
		MinimalRhsFilter filter2 = MinimalRhsFilter.create(lhs2, rhs2);
		assertThat(filter2.asMinimal(candidates)).hasSize(1);
		assertThat(filter2.asMinimal(candidates)).contains(new MDElementImpl(4, 0.5));
	}

	@Test
	public void testNotAffected() {
		LatticeMD latticeMd1 = Mockito.mock(LatticeMD.class);
		LatticeMD latticeMd2 = Mockito.mock(LatticeMD.class);
		int columnPairs = 2;
		MDSite lhs1 = new MDSiteImpl(columnPairs).set(0, 0.5);
		when(latticeMd1.getLhs()).thenReturn(lhs1);
		MDSite lhs2 = new MDSiteImpl(columnPairs).set(1, 0.5);
		when(latticeMd2.getLhs()).thenReturn(lhs2);
		List<MDElement> rhs = Collections.singletonList(new MDElementImpl(3, 0.5));
		IntermediateCandidate candidate1 = new IntermediateCandidate(latticeMd1, rhs);
		IntermediateCandidate candidate2 = new IntermediateCandidate(latticeMd2, rhs);
		Collection<IntermediateCandidate> candidates = Arrays.asList(candidate1, candidate2);
		MinimalRhsFilter filter1 = MinimalRhsFilter.create(lhs1, rhs);
		assertThat(filter1.asMinimal(candidates)).hasSize(1);
		assertThat(filter1.asMinimal(candidates)).contains(new MDElementImpl(3, 0.5));
		MinimalRhsFilter filter2 = MinimalRhsFilter.create(lhs2, rhs);
		assertThat(filter2.asMinimal(candidates)).hasSize(1);
		assertThat(filter2.asMinimal(candidates)).contains(new MDElementImpl(3, 0.5));
	}

}