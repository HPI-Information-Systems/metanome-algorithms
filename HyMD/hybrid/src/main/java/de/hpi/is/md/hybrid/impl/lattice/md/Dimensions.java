package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.util.StreamUtils;
import it.unimi.dsi.fastutil.doubles.Double2IntMap;
import it.unimi.dsi.fastutil.doubles.Double2IntOpenHashMap;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import java.util.List;
import java.util.PrimitiveIterator.OfDouble;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Dimensions implements LevelFunction {

	@NonNull
	private final List<Dimension> dimensions;

	public static LevelFunction of(Iterable<DoubleSortedSet> thresholds) {
		List<Dimension> dimensions = StreamUtils.seq(thresholds)
			.map(Dimension::toDimension)
			.toList();
		return new Dimensions(dimensions);
	}

	@Override
	public int getSingleDistance(int lhsAttr, double threshold) {
		Dimension dimension = dimensions.get(lhsAttr);
		return dimension.getLhsDistance(threshold);
	}

	@Override
	public int size() {
		return dimensions.size();
	}

	@RequiredArgsConstructor
	private static class Dimension {

		@NonNull
		private final Double2IntMap map;

		private static Dimension toDimension(DoubleSortedSet thresholds) {
			int size = thresholds.size();
			Double2IntMap map = new Double2IntOpenHashMap(size);
			int i = 1;
			OfDouble it = thresholds.iterator();
			while (it.hasNext()) {
				double threshold = it.nextDouble();
				map.put(threshold, i++);
			}
			return new Dimension(map);
		}

		private int getLhsDistance(double threshold) {
			return map.get(threshold);
		}
	}
}
