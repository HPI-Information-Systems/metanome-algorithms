package de.metanome.algorithms.dcfinder.helpers;

import java.util.Arrays;
import java.util.Comparator;

public class LongArrayIndexComparator implements Comparator<Integer> {
	public enum Order {
		ASCENDING, DESCENDING
	}

	private final Order order;
	private final long[] array;

	public LongArrayIndexComparator(long[] counts, Order order) {
		this.order = order;
		this.array = counts;
	}

	public Integer[] createIndexArray() {
		Integer[] indexes = new Integer[array.length];
		for (int i = 0; i < array.length; i++) {
			indexes[i] = Integer.valueOf(i);
		}
		Arrays.sort(indexes, this);
		return indexes;
	}

	@Override
	public int compare(Integer index1, Integer index2) {
		switch (order) {
		case ASCENDING:
			return Long.compare(array[index1.intValue()], array[index2.intValue()]);
		case DESCENDING:
			return Long.compare(array[index2.intValue()], array[index1.intValue()]);
		}

		return 0;
	}
}