package de.metanome.algorithms.dcfinder.input.partitions.clusters;

import java.util.List;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;

public class TupleIDProvider {

	private final List<Integer> tIDs;

	public TupleIDProvider(int size) {
		tIDs = IntStream.range(0, size).boxed().collect(ImmutableList.toImmutableList());
	}

	public List<Integer> gettIDs() {
		return tIDs;
	}

}
