package de.hpi.is.md.hybrid.impl.infer;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.md.MDSite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ThresholdLowererTest {

	@Mock
	private LatticeMD latticeMd;
	@Mock
	private MDSite lhs;

	@Test
	public void testMinimal() {
		int rhsAttr = 0;
		double threshold = 0.1;
		ThresholdLowerer task = new ThresholdLowerer(latticeMd);
		when(latticeMd.wouldBeMinimal(new MDElementImpl(rhsAttr, threshold))).thenReturn(true);
		when(latticeMd.getLhs()).thenReturn(lhs);
		task.lowerThreshold(rhsAttr, threshold);
		verify(latticeMd).setRhs(rhsAttr, threshold);
	}

	@Test
	public void testNotMinimal() {
		int rhsAttr = 0;
		double threshold = 0.1;
		ThresholdLowerer task = new ThresholdLowerer(latticeMd);
		when(latticeMd.wouldBeMinimal(new MDElementImpl(rhsAttr, threshold))).thenReturn(false);
		when(latticeMd.getLhs()).thenReturn(lhs);
		task.lowerThreshold(rhsAttr, threshold);
		verify(latticeMd).removeRhs(rhsAttr);
		verify(latticeMd, never()).setRhs(rhsAttr, threshold);
	}

	@Test
	public void testTrivial() {
		int rhsAttr = 0;
		double threshold = 0.1;
		ThresholdLowerer task = new ThresholdLowerer(latticeMd);
		when(latticeMd.wouldBeMinimal(new MDElementImpl(rhsAttr, threshold))).thenReturn(true);
		when(latticeMd.getLhs()).thenReturn(lhs);
		when(lhs.getOrDefault(0)).thenReturn(0.1);
		task.lowerThreshold(rhsAttr, threshold);
		verify(latticeMd).removeRhs(rhsAttr);
		verify(latticeMd, never()).setRhs(rhsAttr, threshold);
	}

}