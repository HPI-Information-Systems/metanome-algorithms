package de.hpi.is.md.util;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static de.hpi.is.md.util.CollectionUtils.ceilingEntry;
import static de.hpi.is.md.util.CollectionUtils.ceilingValue;
import static de.hpi.is.md.util.CollectionUtils.forEach;
import static de.hpi.is.md.util.CollectionUtils.merge;
import static de.hpi.is.md.util.CollectionUtils.mutableSingleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap.Entry;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import it.unimi.dsi.fastutil.doubles.DoubleRBTreeSet;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jooq.lambda.tuple.Tuple;
import org.junit.Assert;
import org.junit.Test;

public class CollectionUtilsTest {

	@Test
	public void forEachDouble2ObjectMap() {
		Double2ObjectMap<String> map = new Double2ObjectOpenHashMap<>();
		Double2ObjectMap<String> target = new Double2ObjectOpenHashMap<>();
		map.put(1.0, "foo");
		map.put(2.0, "bar");
		forEach(map, target::put);
		assertThat(target).hasSize(2);
		assertThat(target).containsEntry(1.0, "foo");
		assertThat(target).containsEntry(2.0, "bar");
	}

	@Test
	public void forEachInt2ObjectMap() {
		Int2ObjectMap<String> map = new Int2ObjectOpenHashMap<>();
		Int2ObjectMap<String> target = new Int2ObjectOpenHashMap<>();
		map.put(1, "foo");
		map.put(2, "bar");
		forEach(map, target::put);
		assertThat(target).hasSize(2);
		assertThat(target).containsEntry(1, "foo");
		assertThat(target).containsEntry(2, "bar");
	}

	@Test
	public void testAsSet() {
		IntSet set = mutableSingleton(1);
		assertThat(set).hasSize(1);
		assertThat(set).contains(1);
		set.add(2);
		assertThat(set).hasSize(2);
		assertThat(set).contains(1, 2);
	}

	@Test
	public void testCeilingEntry() {
		Double2ObjectSortedMap<String> map = new Double2ObjectRBTreeMap<>();
		map.put(0.1, "foo");
		map.put(0.2, "bar");
		assertThat(ceilingEntry(map, 0.05).map(Entry::getDoubleKey)).hasValue(0.1);
		assertThat(ceilingEntry(map, 0.1).map(Entry::getDoubleKey)).hasValue(0.1);
		assertThat(ceilingEntry(map, 0.15).map(Entry::getDoubleKey)).hasValue(0.2);
		assertThat(ceilingEntry(map, 0.2).map(Entry::getDoubleKey)).hasValue(0.2);
		assertThat(ceilingEntry(map, 0.25)).isEmpty();
	}

	@Test
	public void testCeilingValue() {
		Double2ObjectSortedMap<String> map = new Double2ObjectRBTreeMap<>();
		map.put(0.1, "foo");
		map.put(0.2, "bar");
		assertThat(ceilingValue(map, 0.05)).hasValue("foo");
		assertThat(ceilingValue(map, 0.1)).hasValue("foo");
		assertThat(ceilingValue(map, 0.15)).hasValue("bar");
		assertThat(ceilingValue(map, 0.2)).hasValue("bar");
		assertThat(ceilingValue(map, 0.25)).isEmpty();
	}

	@Test
	public void testCrossProduct() {
		assertThat(CollectionUtils.crossProduct(Arrays.asList(1, 2, 3))).hasSize(1 + 2 + 3);
		assertThat(CollectionUtils.crossProduct(Arrays.asList(1, 2, 3)))
			.contains(Tuple.tuple(1, 1));
		assertThat(CollectionUtils.crossProduct(Arrays.asList(1, 2, 3)))
			.contains(Tuple.tuple(1, 2));
		assertThat(CollectionUtils.crossProduct(Arrays.asList(1, 2, 3)))
			.contains(Tuple.tuple(1, 3));
		assertThat(CollectionUtils.crossProduct(Arrays.asList(1, 2, 3)))
			.contains(Tuple.tuple(2, 2));
		assertThat(CollectionUtils.crossProduct(Arrays.asList(1, 2, 3)))
			.contains(Tuple.tuple(2, 3));
		assertThat(CollectionUtils.crossProduct(Arrays.asList(1, 2, 3)))
			.contains(Tuple.tuple(3, 3));
	}

	@Test
	public void testEmptyAfterIntersection() {
		List<IntCollection> clusters = ImmutableList.<IntCollection>builder()
			.add(new IntOpenHashSet(Arrays.asList(1, 2, 3, 4)))
			.add(new IntOpenHashSet(Arrays.asList(2, 3, 4, 5)))
			.add(new IntOpenHashSet(Arrays.asList(1, 3, 4)))
			.add(new IntOpenHashSet(Arrays.asList(1, 5)))
			.build();
		Assert.assertThat(CollectionUtils.intersection(clusters), isPresentAnd(empty()));
	}

	@Test
	public void testFirst() {
		assertThat(CollectionUtils.first(DoubleSortedSets.EMPTY_SET).boxed()).isEmpty();
		assertThat(
			CollectionUtils.first(new DoubleRBTreeSet(Sets.newTreeSet(Arrays.asList(2.0, 1.0))))
				.boxed()).hasValue(1.0);
	}

	@Test
	public void testIntersection() {
		List<IntCollection> clusters = ImmutableList.<IntCollection>builder()
			.add(new IntOpenHashSet(Arrays.asList(1, 2, 3, 4)))
			.add(new IntOpenHashSet(Arrays.asList(2, 3, 4, 5)))
			.add(new IntOpenHashSet(Arrays.asList(1, 3, 4)))
			.build();
		Assert.assertThat(CollectionUtils.intersection(clusters), isPresentAnd(hasSize(2)));
		Assert.assertThat(CollectionUtils.intersection(clusters), isPresentAnd(hasItems(3, 4)));
	}

	@Test
	public void testIntersectionOfEmpty() {
		assertThat(CollectionUtils.intersection(Collections.emptyList())).isEmpty();
	}

	@Test
	public void testMerge() {
		IntSet set1 = mutableSingleton(1);
		IntSet set2 = mutableSingleton(2);
		IntSet merged = merge(set1, set2);
		assertThat(merged).hasSize(2);
		assertThat(merged).contains(1, 2);
		set1.add(3);
		assertThat(set1).hasSize(3);
		assertThat(set1).contains(1, 2, 3);
	}

}