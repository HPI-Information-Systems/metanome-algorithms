package de.hpi.is.md.util;

import com.google.common.collect.ImmutableCollection.Builder;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap.Entry;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMaps;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollectionUtils {

	private static final Comparator<IntCollection> CLUSTER_COMPARATOR = Comparator
		.comparingInt(Collection::size);

	public static <T> List<T> asList(T obj) {
		List<T> list = new ArrayList<>();
		list.add(obj);
		return list;
	}

	public static IntSet mutableSingleton(int i) {
		IntSet set = new IntOpenHashSet();
		set.add(i);
		return set;
	}

	public static <V> Optional<Entry<V>> ceilingEntry(Double2ObjectSortedMap<V> sortedMap,
		double from) {
		Iterable<Entry<V>> entrySet = ceilingEntrySet(sortedMap, from);
		Iterator<Entry<V>> it = entrySet.iterator();
		return IteratorUtils.next(it);
	}

	public static <V> Optional<V> ceilingValue(Double2ObjectSortedMap<V> sortedMap, double from) {
		return ceilingEntry(sortedMap, from)
			.map(Entry::getValue);
	}

	public static <T> Collection<Tuple2<T, T>> crossProduct(List<T> list) {
		Builder<Tuple2<T, T>> result = ImmutableList.builder();
		int size = list.size();
		for (int i = 0; i < size; i++) {
			T left = list.get(i);
			Iterable<T> remaining = list.subList(i, size);
			Seq.of(left)
				.crossJoin(remaining)
				.forEach(result::add);
		}
		return result.build();
	}

	public static OptionalDouble first(DoubleSortedSet set) {
		if (set.isEmpty()) {
			return OptionalDouble.empty();
		}
		double first = set.firstDouble();
		return OptionalDouble.of(first);
	}

	public static <V> void forEach(Double2ObjectMap<V> map, DoubleObjectBiConsumer<V> action) {
		map.double2ObjectEntrySet().forEach(e -> action.accept(e.getDoubleKey(), e.getValue()));
	}

	public static <V> void forEach(Int2ObjectMap<V> map, IntObjectBiConsumer<V> action) {
		map.int2ObjectEntrySet().forEach(e -> action.accept(e.getIntKey(), e.getValue()));
	}

	public static <T> Optional<T> head(List<T> clusters) {
		if (clusters.isEmpty()) {
			return Optional.empty();
		}
		T first = clusters.get(0);
		return Optional.of(first);
	}

	public static Optional<IntCollection> intersection(Collection<IntCollection> clusters) {
		//shrink result as early as possible
		List<IntCollection> sorted = sort(clusters, CLUSTER_COMPARATOR);
		return createIntersector(sorted).map(HeadAndTailIntersector::intersect);
	}

	public static IntSet merge(IntSet left, IntCollection right) {
		left.addAll(right);
		return left;
	}

	public static IntCollection merge(IntCollection left, IntCollection right) {
		left.addAll(right);
		return left;
	}

	public static <T> Collection<T> merge(Collection<T> left, Collection<T> right) {
		left.addAll(right);
		return left;
	}

	public static <T> Predicate<Collection<T>> sizeBelow(int maxSize) {
		return collection -> collection.size() < maxSize;
	}

	public static <T> List<T> sort(Collection<T> collection, Comparator<T> comparator) {
		List<T> list = new ArrayList<>(collection);
		list.sort(comparator);
		return list;
	}

	public static <T> Collection<T> tail(List<T> clusters) {
		int size = clusters.size();
		return clusters.subList(1, size);
	}

	public static <T> Collection<String> toString(Iterable<T> objects) {
		return StreamUtils.seq(objects)
			.map(Objects::toString)
			.toList();
	}

	private static <V> Iterable<Entry<V>> ceilingEntrySet(Double2ObjectSortedMap<V> sortedMap,
		double from) {
		Double2ObjectSortedMap<V> tailMap = sortedMap.tailMap(from);
		return Double2ObjectSortedMaps.fastIterable(tailMap);
	}

	private static Optional<HeadAndTailIntersector> createIntersector(
		List<IntCollection> clusters) {
		return with(clusters).create();
	}

	private static WithClusters with(List<IntCollection> clusters) {
		return new WithClusters(clusters);
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class WithClusters {

		@NonNull
		private final List<IntCollection> clusters;

		private Optional<HeadAndTailIntersector> create() {
			return head(clusters).map(this::create);
		}

		private HeadAndTailIntersector create(IntCollection head) {
			Collection<IntCollection> tail = tail(clusters);
			return HeadAndTailIntersector.create(head, tail);
		}

	}

}
