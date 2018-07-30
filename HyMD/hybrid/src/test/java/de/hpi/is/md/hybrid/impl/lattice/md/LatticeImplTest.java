package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.hybrid.Lattice;
import de.hpi.is.md.hybrid.LatticeTest;

public class LatticeImplTest extends LatticeTest {

	@Override
	protected Lattice createLattice(int columnPairs) {
		return new LatticeImpl(new Cardinality(columnPairs));
	}
}