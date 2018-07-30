package de.hpi.is.md.hybrid.impl.sim.threshold;

import static de.hpi.is.md.util.CollectionUtils.forEach;

import com.bakdata.util.jackson.CPSType;
import de.hpi.is.md.util.Int2Double2ObjectSortedArrayTable;
import de.hpi.is.md.util.Int2Double2ObjectSortedTable;
import it.unimi.dsi.fastutil.doubles.Double2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ThresholdMapArrayFlattener implements ThresholdMapFlattener {

	@NonNull
	private final Double2ObjectSortedMap<IntCollection>[] array;

	static Factory factory() {
		return new FactoryImpl();
	}

	private static Double2ObjectSortedMap<IntCollection> flatten(Double2ObjectMap<IntSet> map) {
		Flattener flattener = new Flattener();
		// thresholds are sorted descending
		forEach(map, flattener::add);
		return flattener.build();
	}

	@Override
	public ThresholdMap build() {
		Int2Double2ObjectSortedTable<IntCollection> table = new Int2Double2ObjectSortedArrayTable<>(
			array);
		return new FlatThresholdMap(table);
	}

	@Override
	public void flatten(int valueId, Double2ObjectMap<IntSet> sortedMap) {
		Double2ObjectSortedMap<IntCollection> flattened = flatten(sortedMap);
		array[valueId] = flattened;
	}

	@RequiredArgsConstructor
	private static class Flattener {

		private final IntCollection current = new IntArrayList();
		private final Double2ObjectSortedMap<IntCollection> flattened = new Double2ObjectAVLTreeMap<>();

		private void add(double threshold, IntCollection value) {
			current.addAll(value);
			IntCollection records = new IntOpenHashSet(current);
			flattened.put(threshold, records);
		}

		private Double2ObjectSortedMap<IntCollection> build() {
			return flattened;
		}
	}

	@CPSType(id = "array", base = Factory.class)
	private static class FactoryImpl implements Factory {

		@SuppressWarnings("unchecked")
		@Override
		public ThresholdMapFlattener create(int size) {
			Double2ObjectSortedMap<IntCollection>[] array = new Double2ObjectSortedMap[size];
			return new ThresholdMapArrayFlattener(array);
		}
	}
}
