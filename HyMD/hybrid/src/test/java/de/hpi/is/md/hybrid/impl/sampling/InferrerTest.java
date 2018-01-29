package de.hpi.is.md.hybrid.impl.sampling;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.SimilaritySet;
import de.hpi.is.md.hybrid.impl.infer.ThresholdLowerer;
import de.hpi.is.md.hybrid.impl.lattice.FullLattice;
import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class InferrerTest {

	@Mock
	private MDSpecializer specializer;
	@Mock
	private FullLattice lattice;
	@Mock
	private SimilaritySet similaritySet;
	@Mock
	private ThresholdLowerer lowerer;

	@Test
	public void test() {
		MDSite lhs = new MDSiteImpl(4);
		Inferrer task = createTask(lhs);
		int rhsAttr = 0;
		MDElement rhs = new MDElementImpl(rhsAttr, 0.5);
		MD specialized = Mockito.mock(MD.class);
		double similarity = 0.4;
		when(similaritySet.get(rhsAttr)).thenReturn(similarity);
		when(specializer.specialize(lhs, rhs, similaritySet))
			.thenReturn(Collections.singletonList(specialized));
		when(lattice.addIfMinimalAndSupported(specialized))
			.thenReturn(Optional.of(Mockito.mock(LatticeMD.class)));
		task.infer(rhs);
		verify(lowerer).lowerThreshold(rhsAttr, 0.4);
		verify(lattice).addIfMinimalAndSupported(specialized);
	}

	@Test
	public void testNotSupported() {
		MDSite lhs = new MDSiteImpl(4);
		Inferrer task = createTask(lhs);
		int rhsAttr = 0;
		MDElement rhs = new MDElementImpl(rhsAttr, 0.5);
		MD specialized = Mockito.mock(MD.class);
		double similarity = 0.4;
		when(similaritySet.get(rhsAttr)).thenReturn(similarity);
		when(specializer.specialize(lhs, rhs, similaritySet))
			.thenReturn(Collections.singletonList(specialized));
		when(lattice.addIfMinimalAndSupported(specialized)).thenReturn(Optional.empty());
		task.infer(rhs);
		verify(lowerer).lowerThreshold(rhsAttr, 0.4);
	}

	private Inferrer createTask(MDSite lhs) {
		return Inferrer.builder()
			.lhs(lhs)
			.lowerer(lowerer)
			.specializer(specializer)
			.fullLattice(lattice)
			.similaritySet(similaritySet)
			.build();
	}

}