package de.hpi.is.md.hybrid.impl.preprocessed;

import static de.hpi.is.md.util.CollectionUtils.mutableSingleton;

import com.google.common.collect.Iterators;
import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.util.CollectionUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapPositionListIndex implements PositionListIndex {

	private static final long serialVersionUID = -9026779893617961286L;
	@NonNull
	private final Int2ObjectMap<IntSet> map;

	public static Builder builder() {
		Int2ObjectOpenHashMap<IntSet> map = new Int2ObjectOpenHashMap<>();
		return new BuilderImpl(map);
	}

	private static Cluster toCluster(Entry<IntSet> e) {
		return new Cluster(e.getIntKey(), e.getValue());
	}

	@Override
	public IntSet get(int valueId) {
		IntSet records = map.get(valueId);
		return Optional.ofNullable(records)
			.orElse(IntSets.EMPTY_SET);
	}

	@Override
	public Iterator<Cluster> iterator() {
		Set<Entry<IntSet>> entries = entries();
		return Iterators.transform(entries.iterator(), MapPositionListIndex::toCluster);
	}

	@Override
	public Spliterator<Cluster> spliterator() {
		Set<Entry<IntSet>> entries = entries();
		return entries.stream()
			.map(MapPositionListIndex::toCluster)
			.spliterator();
	}

	private Set<Entry<IntSet>> entries() {
		return map.int2ObjectEntrySet();
	}

	@RequiredArgsConstructor
	private static class BuilderImpl implements Builder {

		@NonNull
		private final Int2ObjectMap<IntSet> map;

		@Override
		public void add(int recordId, int value) {
			IntSet set = mutableSingleton(recordId);
			map.merge(value, set, CollectionUtils::merge);
		}

		@Override
		public PositionListIndex build() {
			return new MapPositionListIndex(map);
		}
	}

}
