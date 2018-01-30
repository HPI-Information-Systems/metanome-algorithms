package de.hpi.is.md.hybrid;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArrayDictionaryRecords implements DictionaryRecords {

	private static final long serialVersionUID = -7960775473355559810L;
	@NonNull
	private final int[][] records;

	public static Builder builder() {
		return new BuilderImpl();
	}

	@Override
	public int[] get(int id) {
		return records[id];
	}

	@Override
	public IntSet getAll() {
		IntSet all = new IntOpenHashSet(records.length);
		IntStream.range(0, records.length).forEach(all::add);
		return all;
	}

	@Override
	public Iterator<int[]> iterator() {
		return asIterable().iterator();
	}

	@Override
	public Spliterator<int[]> spliterator() {
		return asIterable().spliterator();
	}

	private Iterable<int[]> asIterable() {
		return Arrays.asList(records);
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class BuilderImpl implements Builder {

		@NonNull
		private final Int2ObjectMap<int[]> records = new Int2ObjectOpenHashMap<>();

		@Override
		public Builder add(int recordId, int[] record) {
			records.put(recordId, record);
			return this;
		}

		@Override
		public DictionaryRecords build() {
			int size = records.size();
			int[][] array = new int[size][];
			for (Entry<int[]> entry : records.int2ObjectEntrySet()) {
				int id = entry.getIntKey();
				array[id] = entry.getValue();
			}
			return new ArrayDictionaryRecords(array);
		}
	}
}
