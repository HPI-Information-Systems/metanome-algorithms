package de.hpi.is.md.hybrid.impl.level.minimizing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.Rhs;
import de.hpi.is.md.hybrid.impl.level.Candidate;
import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class CandidateTest {

	@Mock
	private LatticeMD latticeMd;

	@Test
	public void tesForEach() {
		MDSite lhs = new MDSiteImpl(4).set(2, 0.4);
		when(latticeMd.getLhs()).thenReturn(lhs);
		Candidate md = new Candidate(latticeMd,
			Arrays.asList(new MDElementImpl(0, 0.5), new MDElementImpl(1, 0.6)));
		Multimap<MDSite, MDElement> map = HashMultimap.create();
		md.forEach(map::put);
		org.assertj.guava.api.Assertions.assertThat(map).hasSize(2);
		assertThat(map.get(lhs)).hasSize(2);
		assertThat(map.get(lhs)).contains(new MDElementImpl(0, 0.5));
		assertThat(map.get(lhs)).contains(new MDElementImpl(1, 0.6));
	}

	@Test
	public void testGetRhs() {
		when(latticeMd.getMaxGenThresholds(new int[]{0, 1})).thenReturn(new double[]{0.4, 0.5});
		Candidate md = new Candidate(latticeMd,
			Arrays.asList(new MDElementImpl(0, 0.5), new MDElementImpl(1, 0.6)));
		assertThat(md.getRhs()).hasSize(2);
		assertThat(md.getRhs())
			.contains(Rhs.builder().rhsAttr(0).threshold(0.5).lowerBound(0.4).build());
		assertThat(md.getRhs())
			.contains(Rhs.builder().rhsAttr(1).threshold(0.6).lowerBound(0.5).build());
	}

}