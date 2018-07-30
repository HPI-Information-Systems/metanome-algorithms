package de.hpi.is.md.hybrid.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.hybrid.PositionListIndex.Cluster;
import de.hpi.is.md.hybrid.PreprocessedColumnPair;
import de.hpi.is.md.util.StreamUtils;
import de.hpi.is.md.util.TupleUtils;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RecordGrouperImpl implements RecordGrouper {

	@NonNull
	private final DictionaryRecords leftRecords;
	@NonNull
	private final PositionListIndex first;
	@NonNull
	private final Collection<PreprocessedColumnPair> remaining;

	@Override
	public Seq<Tuple2<Selector, Collection<int[]>>> buildSelectors() {
		return StreamUtils.seq(first)
			.flatMap(this::group);
	}

	public WithValue with(Cluster cluster) {
		int value = cluster.getValue();
		IntSet records = cluster.getRecords();
		return new WithValue(value, HashMultimap.create(records.size(), 1));
	}

	private Stream<Tuple2<Selector, Collection<int[]>>> group(Cluster clusters) {
		IntSet records = clusters.getRecords();
		return with(clusters).process(records);
	}

	@RequiredArgsConstructor
	private class WithValue {

		private final int value;
		private final Multimap<Selector, int[]> groupedRecords;

		private Stream<Tuple2<Selector, Collection<int[]>>> getGroups() {
			Map<Selector, Collection<int[]>> map = groupedRecords.asMap();
			Set<Map.Entry<Selector, Collection<int[]>>> entries = map.entrySet();
			return StreamUtils.seq(entries)
				.map(TupleUtils::toTuple);
		}

		private Selector getSelector(int[] record) {
			int[] values = getValues(record);
			return new Selector(values);
		}

		private int[] getValues(int[] record) {
			int[] values = newValues();
			int i = 1;
			for (PreprocessedColumnPair columnPair : remaining) {
				values[i++] = columnPair.getLeftValue(record);
			}
			return values;
		}

		private int[] newValues() {
			int[] values = new int[1 + remaining.size()];
			values[0] = value;
			return values;
		}

		private Stream<Tuple2<Selector, Collection<int[]>>> process(IntIterable records) {
			records.forEach((IntConsumer) this::process);
			return getGroups();
		}

		private void process(int recordId) {
			int[] record = leftRecords.get(recordId);
			Selector selector = getSelector(record);
			groupedRecords.put(selector, record);
		}
	}

}
