package de.hpi.is.md.hybrid;

import static org.assertj.core.api.Assertions.assertThat;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.impl.md.MDImpl;
import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.sim.SimilarityMeasure;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import org.junit.Test;

public abstract class LatticeTest {

	@Test
	public void testAdd() {
		int columnPairs = 4;
		Lattice lattice = createLattice(columnPairs);
		MDElement rhs = new MDElementImpl(3, 0.5);
		MDSite lhs = new MDSiteImpl(columnPairs).set(0, 0.7);
		MD md1 = new MDImpl(lhs, rhs);
		MD md2 = new MDImpl(lhs, new MDElementImpl(2, 0.6));
		LatticeMD latticeMd = lattice.add(md1);
		assertThat(latticeMd.getRhs()).hasSize(1);
		lattice.add(md2);
		assertThat(latticeMd.getRhs()).hasSize(2);
		assertThat(latticeMd.getLhs()).isEqualTo(lhs);
	}

	@Test
	public void testAddIfMinimal() {
		int columnPairs = 4;
		Lattice lattice = createLattice(columnPairs);
		MDElement rhs = new MDElementImpl(3, 0.5);
		MDSite lhs = new MDSiteImpl(columnPairs).set(0, 0.7);
		MD md1 = new MDImpl(lhs, rhs);
		Optional<LatticeMD> groupedMd = lattice.addIfMinimal(md1);
		assertThat(groupedMd).isPresent();
		assertThat(lattice.addIfMinimal(md1)).isEmpty();
	}

	@Test
	public void testAddMultiple() {
		int columnPairs = 4;
		Lattice lattice = createLattice(columnPairs);
		MDSite lhs = new MDSiteImpl(columnPairs).set(0, 0.7);
		MD md1 = new MDImpl(lhs, new MDElementImpl(3, 0.5));
		MD md2 = new MDImpl(lhs, new MDElementImpl(3, 0.6));
		MD md3 = new MDImpl(lhs, new MDElementImpl(3, 0.4));
		LatticeMD latticeMd = lattice.add(md1);
		assertThat(latticeMd.getRhs()).contains(new MDElementImpl(3, 0.5));
		lattice.add(md2);
		assertThat(latticeMd.getRhs()).contains(new MDElementImpl(3, 0.5));
		lattice.add(md3);
		assertThat(latticeMd.getRhs()).contains(new MDElementImpl(3, 0.4));
	}

	@Test
	public void testContainsMdOrGeneralization() {
		int columnPairs = 4;
		Lattice lattice = createLattice(columnPairs);
		MDElement rhs = new MDElementImpl(3, 0.5);
		MDSite lhs = new MDSiteImpl(columnPairs)
			.set(0, 0.7);
		MD md = new MDImpl(lhs, rhs);
		assertThat(lattice.containsMdOrGeneralization(md)).isFalse();
		lattice.add(md);
		MDSite specLhs = lhs.clone()
			.set(1, 0.8);
		MD spec = new MDImpl(specLhs, rhs);
		assertThat(lattice.containsMdOrGeneralization(md)).isTrue();
		assertThat(lattice.containsMdOrGeneralization(spec)).isTrue();
		MDElement genRhs = new MDElementImpl(3, 0.6);
		assertThat(lattice.containsMdOrGeneralization(new MDImpl(specLhs, genRhs))).isFalse();
		MDElement specRhs = new MDElementImpl(3, 0.4);
		assertThat(lattice.containsMdOrGeneralization(new MDImpl(specLhs, specRhs))).isTrue();
	}

	@Test
	public void testFindViolated() {
		int columnPairs = 4;
		Lattice lattice = createLattice(columnPairs);
		lattice.add(new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.4), new MDElementImpl(1, 0.6)));
		lattice.add(new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.4), new MDElementImpl(2, 0.6)));
		lattice.add(new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.4), new MDElementImpl(3, 0.5)));
		lattice.add(new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.4).set(1, 0.6),
			new MDElementImpl(2, 0.7)));
		lattice.add(new MDImpl(new MDSiteImpl(columnPairs).set(1, 0.4), new MDElementImpl(2, 0.5)));
		lattice.add(new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.4).set(1, 0.4),
			new MDElementImpl(3, 0.7)));
		SimilaritySet similaritySet = new SimilaritySet(new double[]{0.5, 0.5, 0.5, 0.5});
		Collection<LatticeMD> violated = lattice.findViolated(similaritySet);
		assertThat(violated).hasSize(2);
		Iterator<LatticeMD> it = violated.iterator();
		assertThat(it.next().getLhs()).isIn(new MDSiteImpl(columnPairs).set(0, 0.4),
			new MDSiteImpl(columnPairs).set(0, 0.4).set(1, 0.4));
		assertThat(it.next().getLhs()).isIn(new MDSiteImpl(columnPairs).set(0, 0.4),
			new MDSiteImpl(columnPairs).set(0, 0.4).set(1, 0.4));
	}

	@Test
	public void testGetLevel() {
		int columnPairs = 4;
		Lattice lattice = createLattice(columnPairs);
		MDElement rhs = new MDElementImpl(3, 0.5);
		MDSite lhs = new MDSiteImpl(columnPairs).set(0, 0.7);
		MD md1 = new MDImpl(lhs, rhs);
		MD md2 = new MDImpl(new MDSiteImpl(columnPairs).set(1, 0.6), rhs);
		MD md3 = new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.6).set(1, 0.5), rhs);
		MD md4 = new MDImpl(lhs, new MDElementImpl(2, 0.6));
		lattice.add(md1);
		lattice.add(md2);
		lattice.add(md3);
		lattice.add(md4);
		assertThat(lattice.getDepth()).isEqualTo(2);
		assertThat(lattice.getLevel(0)).isEmpty();
		assertThat(lattice.getLevel(1)).hasSize(2);
		assertThat(lattice.getLevel(2)).hasSize(1);
	}

	@Test
	public void testIsMinimal() {
		int columnPairs = 4;
		Lattice lattice = createLattice(columnPairs);
		MDElement rhs = new MDElementImpl(3, 0.5);
		MDSite lhs = new MDSiteImpl(columnPairs)
			.set(0, 0.7);
		MD md = new MDImpl(lhs, rhs);
		assertThat(lattice.containsMdOrGeneralization(md)).isFalse();
		lattice.add(md);
		MDSite specLhs = lhs.clone()
			.set(1, 0.8);
		LatticeMD latticeMd = lattice.add(new MDImpl(specLhs, new MDElementImpl(3, 0.6)));
		assertThat(latticeMd.wouldBeMinimal(new MDElementImpl(3, 0.6))).isTrue();
		assertThat(latticeMd.wouldBeMinimal(new MDElementImpl(3, 0.5))).isFalse();
		assertThat(latticeMd.wouldBeMinimal(new MDElementImpl(3, 0.4))).isFalse();
	}

	@Test
	public void testMaxGenThreshold() {
		int columnPairs = 4;
		Lattice lattice = createLattice(columnPairs);
		lattice.add(new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.7), new MDElementImpl(3, 0.5)));
		assertThat(lattice
			.getMaxThresholds(new MDSiteImpl(columnPairs).set(0, 0.8).set(1, 0.8), new int[]{3}))
			.isEqualTo(new double[]{0.5});
		LatticeMD latticeMd = lattice.add(
			new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.8).set(1, 0.8),
				new MDElementImpl(3, 0.6)));
		assertThat(lattice
			.getMaxThresholds(new MDSiteImpl(columnPairs).set(0, 0.8).set(1, 0.8), new int[]{3}))
			.isEqualTo(new double[]{0.6});
		assertThat(latticeMd.getMaxGenThresholds(new int[]{3})).isEqualTo(new double[]{0.5});
	}

	@Test
	public void testMaxThreshold() {
		int columnPairs = 4;
		Lattice lattice = createLattice(columnPairs);
		lattice.add(new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.7), new MDElementImpl(3, 0.5)));
		assertThat(lattice
			.getMaxThresholds(new MDSiteImpl(columnPairs).set(0, 0.8).set(1, 0.8), new int[]{3}))
			.isEqualTo(new double[]{0.5});
		assertThat(lattice
			.getMaxThresholds(new MDSiteImpl(columnPairs).set(0, 0.8).set(1, 0.8).set(2, 0.8),
				new int[]{3}))
			.isEqualTo(new double[]{0.5});
		lattice.add(new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.8), new MDElementImpl(3, 0.6)));
		assertThat(lattice
			.getMaxThresholds(new MDSiteImpl(columnPairs).set(0, 0.8).set(1, 0.8), new int[]{3}))
			.isEqualTo(new double[]{0.6});
		assertThat(lattice
			.getMaxThresholds(new MDSiteImpl(columnPairs).set(0, 0.8).set(1, 0.8).set(2, 0.8),
				new int[]{3}))
			.isEqualTo(new double[]{0.6});
		lattice.add(new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.7).set(1, 0.7),
			new MDElementImpl(3, 0.7)));
		assertThat(lattice
			.getMaxThresholds(new MDSiteImpl(columnPairs).set(0, 0.8).set(1, 0.8), new int[]{3}))
			.isEqualTo(new double[]{0.7});
		assertThat(lattice
			.getMaxThresholds(new MDSiteImpl(columnPairs).set(0, 0.8).set(1, 0.8).set(2, 0.8),
				new int[]{3}))
			.isEqualTo(new double[]{0.7});
		lattice.add(new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.8).set(2, 0.8),
			new MDElementImpl(3, 0.8)));
		assertThat(lattice
			.getMaxThresholds(new MDSiteImpl(columnPairs).set(0, 0.8).set(1, 0.8), new int[]{3}))
			.isEqualTo(new double[]{0.7});
		assertThat(lattice
			.getMaxThresholds(new MDSiteImpl(columnPairs).set(0, 0.8).set(1, 0.8).set(2, 0.8),
				new int[]{3}))
			.isEqualTo(new double[]{0.8});
		assertThat(lattice.getMaxThresholds(new MDSiteImpl(columnPairs).set(0, 0.1), new int[]{3}))
			.isEqualTo(new double[]{SimilarityMeasure.MIN_SIMILARITY});
		lattice.add(new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.9).set(2, 0.9),
			new MDElementImpl(1, 1.0)));
		assertThat(lattice
			.getMaxThresholds(new MDSiteImpl(columnPairs).set(0, 1.0).set(2, 1.0).set(3, 1.0),
				new int[]{1}))
			.isEqualTo(new double[]{1.0});
	}

	@Test
	public void testRemove() {
		int columnPairs = 4;
		Lattice lattice = createLattice(columnPairs);
		MDElement rhs = new MDElementImpl(3, 0.5);
		MDSite lhs = new MDSiteImpl(columnPairs).set(0, 0.7);
		MD md1 = new MDImpl(lhs, rhs);
		MD md2 = new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.6).set(1, 0.5), rhs);
		MD md3 = new MDImpl(lhs, new MDElementImpl(2, 0.6));
		lattice.add(md1);
		lattice.add(md2);
		lattice.add(md3);
		LatticeMD level1 = lattice.getLevel(1).iterator().next();
		level1.removeRhs(3);
		LatticeMD level2 = lattice.getLevel(2).iterator().next();
		level2.removeRhs(3);
		assertThat(lattice.getLevel(0)).isEmpty();
		assertThat(lattice.getLevel(1)).hasSize(1);
		assertThat(lattice.getLevel(2)).isEmpty();
		assertThat(lattice.containsMdOrGeneralization(md1)).isFalse();
		assertThat(lattice.containsMdOrGeneralization(md2)).isFalse();
		assertThat(lattice.containsMdOrGeneralization(md3)).isTrue();
	}

	@Test
	public void testSet() {
		int columnPairs = 4;
		Lattice lattice = createLattice(columnPairs);
		MDElement rhs = new MDElementImpl(3, 0.5);
		MDSite lhs = new MDSiteImpl(columnPairs).set(0, 0.7);
		MD md1 = new MDImpl(lhs, rhs);
		MD md2 = new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.6).set(1, 0.5), rhs);
		MD md3 = new MDImpl(lhs, new MDElementImpl(2, 0.6));
		lattice.add(md1);
		lattice.add(md2);
		lattice.add(md3);
		LatticeMD level1 = lattice.getLevel(1).iterator().next();
		level1.setRhs(3, 0.4);
		LatticeMD level2 = lattice.getLevel(2).iterator().next();
		level2.setRhs(3, 0.6);
		assertThat(lattice.getLevel(0)).isEmpty();
		assertThat(lattice.getLevel(1)).hasSize(1);
		assertThat(lattice.getLevel(2)).hasSize(1);
		assertThat(lattice.containsMdOrGeneralization(md1)).isFalse();
		assertThat(lattice.containsMdOrGeneralization(md2)).isTrue();
		assertThat(lattice.containsMdOrGeneralization(md3)).isTrue();
	}

	protected abstract Lattice createLattice(int columnPairs);

}
