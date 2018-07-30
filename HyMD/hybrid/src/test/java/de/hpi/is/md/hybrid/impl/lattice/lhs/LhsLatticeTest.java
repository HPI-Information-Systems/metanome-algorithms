package de.hpi.is.md.hybrid.impl.lattice.lhs;

import static org.assertj.core.api.Assertions.assertThat;

import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import org.junit.Test;

public class LhsLatticeTest {

	@Test
	public void test() {
		int columnPairs = 4;
		LhsLattice lattice = new LhsLattice(columnPairs);
		lattice.addIfMinimal(new MDSiteImpl(columnPairs).set(0, 0.6).set(1, 0.7));
		assertThat(
			lattice.containsMdOrGeneralization(new MDSiteImpl(columnPairs).set(0, 0.6).set(1, 0.7)))
			.isEqualTo(true);
		assertThat(lattice.containsMdOrGeneralization(new MDSiteImpl(columnPairs).set(0, 0.6)))
			.isEqualTo(false);
		assertThat(
			lattice.containsMdOrGeneralization(new MDSiteImpl(columnPairs).set(0, 0.7).set(1, 0.7)))
			.isEqualTo(true);
		assertThat(
			lattice.containsMdOrGeneralization(new MDSiteImpl(columnPairs).set(0, 0.7).set(1, 0.6)))
			.isEqualTo(false);
		assertThat(lattice.containsMdOrGeneralization(
			new MDSiteImpl(columnPairs).set(0, 0.6).set(1, 0.7).set(2, 0.8))).isEqualTo(true);
	}

}