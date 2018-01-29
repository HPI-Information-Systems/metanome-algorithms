package de.hpi.is.md.hybrid;

import static org.assertj.core.api.Assertions.assertThat;

import de.hpi.is.md.hybrid.impl.lattice.md.Cardinality;
import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.impl.md.MDImpl;
import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.sim.SimilarityMeasure;
import org.junit.Test;

public class LatticeHelperTest {

	@Test
	public void testCreate() {
		int columnPairs = 4;
		Lattice lattice = LatticeHelper.createLattice(new Cardinality(columnPairs));
		assertThat(lattice.getDepth()).isEqualTo(0);
		assertThat(lattice.getLevel(0)).hasSize(1);
		assertThat(lattice.containsMdOrGeneralization(
			new MDImpl(new MDSiteImpl(columnPairs),
				new MDElementImpl(0, SimilarityMeasure.MAX_SIMILARITY)))).isEqualTo(true);
		assertThat(lattice.containsMdOrGeneralization(
			new MDImpl(new MDSiteImpl(columnPairs),
				new MDElementImpl(1, SimilarityMeasure.MAX_SIMILARITY)))).isEqualTo(true);
		assertThat(lattice.containsMdOrGeneralization(
			new MDImpl(new MDSiteImpl(columnPairs),
				new MDElementImpl(2, SimilarityMeasure.MAX_SIMILARITY)))).isEqualTo(true);
		assertThat(lattice.containsMdOrGeneralization(
			new MDImpl(new MDSiteImpl(columnPairs),
				new MDElementImpl(3, SimilarityMeasure.MAX_SIMILARITY)))).isEqualTo(true);
	}

}
