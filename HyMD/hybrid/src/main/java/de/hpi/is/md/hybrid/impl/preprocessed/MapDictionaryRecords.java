package de.hpi.is.md.hybrid.impl.preprocessed;

import de.hpi.is.md.hybrid.DictionaryRecords;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Iterator;
import java.util.Spliterator;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapDictionaryRecords implements DictionaryRecords {

	private static final long serialVersionUID = -964690621883283588L;
	@NonNull
	private final Int2ObjectMap<int[]> records;

	public static Builder builder() {
		return new BuilderImpl();
	}

	@Override
	public int[] get(int id) {
		return records.get(id);
	}

	@Override
	public IntSet getAll() {
		return records.keySet();
	}

	@Override
	public Iterator<int[]> iterator() {
		return values().iterator();
	}

	@Override
	public Spliterator<int[]> spliterator() {
		return values().spliterator();
	}

	private Iterable<int[]> values() {
		return records.values();
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class BuilderImpl implements Builder {

		@NonNull
		private final Int2ObjectMap<int[]> records = new Int2ObjectOpenHashMap<>();

		@Override
		public BuilderImpl add(int recordId, int[] record) {
			records.put(recordId, record);
			return this;
		}

		@Override
		public DictionaryRecords build() {
			return new MapDictionaryRecords(records);
		}
	}
}
