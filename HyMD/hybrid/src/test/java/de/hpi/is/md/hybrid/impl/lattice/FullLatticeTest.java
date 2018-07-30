package de.hpi.is.md.hybrid.impl.lattice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hpi.is.md.hybrid.Lattice;
import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.SimilaritySet;
import de.hpi.is.md.hybrid.impl.lattice.lhs.LhsLattice;
import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.impl.md.MDImpl;
import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class FullLatticeTest {

	@Mock
	private Lattice lattice;
	@Mock
	private LhsLattice notSupported;

	@Test
	public void testAddNotSupported() {
		FullLattice fullLattice = new FullLattice(lattice, notSupported);
		int columnPairs = 4;
		MDSite lhs = new MDSiteImpl(columnPairs).set(0, 0.5);
		when(notSupported.containsMdOrGeneralization(lhs)).thenReturn(true);
		MD md = new MDImpl(lhs, new MDElementImpl(1, 0.7));
		Optional<LatticeMD> latticeMD = fullLattice.addIfMinimalAndSupported(md);
		assertThat(latticeMD).isEmpty();
		verify(lattice, never()).addIfMinimal(md);
	}

	@Test
	public void testAddSupported() {
		FullLattice fullLattice = new FullLattice(lattice, notSupported);
		int columnPairs = 4;
		MDSite lhs = new MDSiteImpl(columnPairs).set(0, 0.5);
		when(notSupported.containsMdOrGeneralization(lhs)).thenReturn(false);
		MD md = new MDImpl(lhs, new MDElementImpl(1, 0.7));
		LatticeMD latticeMD = Mockito.mock(LatticeMD.class);
		when(lattice.addIfMinimal(md)).thenReturn(Optional.of(latticeMD));
		assertThat(fullLattice.addIfMinimalAndSupported(md)).hasValue(latticeMD);
		verify(lattice).addIfMinimal(md);
	}

	@Test
	public void testFindViolated() {
		FullLattice fullLattice = new FullLattice(lattice, notSupported);
		LatticeMD latticeMD = Mockito.mock(LatticeMD.class);
		SimilaritySet similaritySet = Mockito.mock(SimilaritySet.class);
		when(lattice.findViolated(similaritySet)).thenReturn(Collections.singletonList(latticeMD));
		Collection<LatticeMD> violated = fullLattice.findViolated(similaritySet);
		assertThat(violated).hasSize(1);
		assertThat(violated).contains(latticeMD);
		verify(lattice).findViolated(similaritySet);
	}

	@Test
	public void testGetDepth() {
		FullLattice fullLattice = new FullLattice(lattice, notSupported);
		when(lattice.getDepth()).thenReturn(2);
		assertThat(fullLattice.getDepth()).isEqualTo(2);
		verify(lattice).getDepth();
	}

	@Test
	public void testGetLevel() {
		FullLattice fullLattice = new FullLattice(lattice, notSupported);
		LatticeMD latticeMD = Mockito.mock(LatticeMD.class);
		when(lattice.getLevel(1)).thenReturn(Collections.singletonList(latticeMD));
		Collection<LatticeMD> level = fullLattice.getLevel(1);
		assertThat(level).hasSize(1);
		assertThat(level).contains(latticeMD);
		verify(lattice).getLevel(1);
	}

	@Test
	public void testMarkNotSupported() {
		FullLattice fullLattice = new FullLattice(lattice, notSupported);
		int columnPairs = 4;
		MDSite lhs = new MDSiteImpl(columnPairs).set(0, 0.5);
		fullLattice.markNotSupported(lhs);
		verify(notSupported).addIfMinimal(lhs);
	}

}