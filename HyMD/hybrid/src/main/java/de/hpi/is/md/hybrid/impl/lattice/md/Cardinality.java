package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.hybrid.md.MDSite;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Cardinality implements LevelFunction {

	private final int size;

	public static Factory factory() {
		return new FactoryImpl();
	}

	@Override
	public int getDistance(MDSite lhs) {
		return lhs.cardinality();
	}

	@Override
	public int getSingleDistance(int lhsAttr, double threshold) {
		return 1;
	}

	@Override
	public int size() {
		return size;
	}

	private static class FactoryImpl implements Factory {

		@Override
		public LevelFunction create(List<DoubleSortedSet> thresholds) {
			int size = thresholds.size();
			return new Cardinality(size);
		}
	}
}
