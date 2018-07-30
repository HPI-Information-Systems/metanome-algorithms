package de.hpi.is.md.hybrid.impl.md;

import de.hpi.is.md.hybrid.MDTest;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;

public class MDImplTest extends MDTest {

	@Override
	protected MD createMD(MDSite lhs, MDElement rhs) {
		return new MDImpl(lhs, rhs);
	}

	@Override
	protected MDElement createMDElement(int attr, double threshold) {
		return new MDElementImpl(attr, threshold);
	}

	@Override
	protected MDSite createMDSite(int columnPairs) {
		return new MDSiteImpl(columnPairs);
	}

}