package de.hpi.is.md.hybrid.impl.sampling;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.SimilaritySet;
import de.hpi.is.md.hybrid.impl.lattice.FullLattice;
import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SimilaritySetProcessorTest {

	@Mock
	private FullLattice lattice;
	@Mock
	private MDSpecializer specializer;

	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		SimilaritySetProcessor processor = createProcessor();
		SimilaritySet similaritySet = Mockito.mock(SimilaritySet.class);
		LatticeMD latticeMD = Mockito.mock(LatticeMD.class);
		MDSite lhs = Mockito.mock(MDSite.class);
		when(latticeMD.getLhs()).thenReturn(lhs);
		when(lattice.findViolated(similaritySet))
			.thenReturn(Collections.singletonList(latticeMD), Collections.emptyList());
		int rhsAttr = 0;
		MDElement rhs = new MDElementImpl(rhsAttr, 0.5);
		when(latticeMD.getRhs()).thenReturn(Collections.singletonList(rhs));
		when(Boolean.valueOf(similaritySet.isViolated(rhs))).thenReturn(Boolean.TRUE);
		when(Double.valueOf(similaritySet.get(rhsAttr))).thenReturn(Double.valueOf(0.4));
		when(specializer.specialize(lhs, rhs, similaritySet)).thenReturn(Collections.emptyList());
		when(Boolean.valueOf(latticeMD.wouldBeMinimal(new MDElementImpl(rhsAttr, 0.4)))).thenReturn(Boolean.FALSE);
		processor.process(similaritySet);
		verify(latticeMD).removeRhs(rhsAttr);
		verify(lattice, never()).addIfMinimalAndSupported(any());
	}

	private SimilaritySetProcessor createProcessor() {
		return new SimilaritySetProcessor(lattice, specializer);
	}

}