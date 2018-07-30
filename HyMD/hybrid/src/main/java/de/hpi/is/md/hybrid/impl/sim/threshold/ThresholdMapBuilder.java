package de.hpi.is.md.hybrid.impl.sim.threshold;

import static de.hpi.is.md.util.CollectionUtils.forEach;
import static it.unimi.dsi.fastutil.doubles.DoubleComparators.OPPOSITE_COMPARATOR;

import de.hpi.is.md.hybrid.impl.sim.PreprocessedSimilarity.To;
import de.hpi.is.md.util.CollectionUtils;
import it.unimi.dsi.fastutil.doubles.Double2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

class ThresholdMapBuilder {

	private final Int2ObjectMap<Double2ObjectSortedMap<IntSet>> map = new Int2ObjectOpenHashMap<>();

	ThresholdMapBuilder add(int left, Iterable<To> similarities) {
		RowBuilder rowBuilder = RowBuilder.emptyRow();
		similarities.forEach(rowBuilder::process);
		Double2ObjectSortedMap<IntSet> thresholdRow = rowBuilder.getRow();
		map.put(left, thresholdRow);
		return this;
	}

	ThresholdMap build(ThresholdMapFlattener flattener) {
		forEach(map, flattener::flatten);
		return flattener.build();
	}

	ThresholdMap build() {
		return new CollectingThresholdMap(map);
	}

	@RequiredArgsConstructor
	private static class RowBuilder {

		@Getter
		@NonNull
		private final Double2ObjectSortedMap<IntSet> row;

		private static RowBuilder emptyRow() {
			//comparator needs to be Serializable
			Double2ObjectSortedMap<IntSet> row = new Double2ObjectAVLTreeMap<>(OPPOSITE_COMPARATOR);
			return new RowBuilder(row);
		}

		private void process(To to) {
			double sim = to.getSimilarity();
			IntSet records = to.getRecords();
			IntSet value = new IntOpenHashSet(records);
			row.merge(sim, value, CollectionUtils::merge);
		}
	}
}
