package de.hpi.is.md.hybrid.impl.preprocessed;

import static de.hpi.is.md.util.CollectionUtils.mutableSingleton;

import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.util.CollectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiConsumer;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArrayPositionListIndex implements PositionListIndex {

	private static final long serialVersionUID = 4441131985840473264L;
	@NonNull
	private final IntSet[] array;

	public static BuilderImpl builder() {
		Int2ObjectOpenHashMap<IntSet> map = new Int2ObjectOpenHashMap<>();
		return new BuilderImpl(map);
	}

	@Override
	public IntSet get(int valueId) {
		if (valueId >= array.length) {
			return IntSets.EMPTY_SET;
		}
		return array[valueId];
	}

	@Override
	public Iterator<Cluster> iterator() {
		return new ClusterIterator();
	}

	@RequiredArgsConstructor
	private static class BuilderImpl implements Builder {

		@NonNull
		private final Int2ObjectMap<IntSet> map;
		private int maxValue;

		private static <T> BiConsumer<Integer, T> set(T[] array) {
			return (k, v) -> array[k] = v;
		}

		@Override
		public void add(int recordId, int value) {
			updateMaxValue(value);
			IntSet set = mutableSingleton(recordId);
			map.merge(value, set, CollectionUtils::merge);
		}

		@Override
		public PositionListIndex build() {
			IntSet[] array = new IntSet[maxValue + 1];
			Arrays.fill(array, IntSets.EMPTY_SET);
			map.forEach(set(array));
			return new ArrayPositionListIndex(array);
		}

		private void updateMaxValue(int value) {
			maxValue = Math.max(value, maxValue);
		}
	}

	private class ClusterIterator implements Iterator<Cluster> {

		private int currentIndex = 0;

		@Override
		public boolean hasNext() {
			return currentIndex < array.length;
		}

		@Override
		public Cluster next() {
			return new Cluster(currentIndex, array[currentIndex++]);
		}
	}
}
