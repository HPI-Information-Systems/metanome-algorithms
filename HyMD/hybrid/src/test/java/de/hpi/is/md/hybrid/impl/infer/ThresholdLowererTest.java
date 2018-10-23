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
		when(Boolean.valueOf(latticeMd.wouldBeMinimal(new MDElementImpl(rhsAttr, threshold)))).thenReturn(Boolean.TRUE);
		when(latticeMd.getLhs()).thenReturn(lhs);
		task.lowerThreshold(rhsAttr, threshold);
		verify(latticeMd).setRhs(rhsAttr, threshold);
	}

	@Test
	public void testNotMinimal() {
		int rhsAttr = 0;
		double threshold = 0.1;
		ThresholdLowerer task = new ThresholdLowerer(latticeMd);
		when(Boolean.valueOf(latticeMd.wouldBeMinimal(new MDElementImpl(rhsAttr, threshold)))).thenReturn(Boolean.FALSE);
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
		when(Boolean.valueOf(latticeMd.wouldBeMinimal(new MDElementImpl(rhsAttr, threshold)))).thenReturn(Boolean.TRUE);
		when(latticeMd.getLhs()).thenReturn(lhs);
		when(Double.valueOf(lhs.getOrDefault(0))).thenReturn(Double.valueOf(0.1));
		task.lowerThreshold(rhsAttr, threshold);
		verify(latticeMd).removeRhs(rhsAttr);
		verify(latticeMd, never()).setRhs(rhsAttr, threshold);
	}

}