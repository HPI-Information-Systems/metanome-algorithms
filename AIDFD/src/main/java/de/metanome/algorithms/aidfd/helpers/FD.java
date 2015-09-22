package de.metanome.algorithms.aidfd.helpers;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;

public class FD implements Comparable<FD> {
	public final int rhs;

	public final IBitSet lhs;
	public IBitSet lhsSort;

	public FD(int rhs, IBitSet lhs) {
		this.rhs = rhs;
		this.lhs = lhs;
		this.lhsSort = lhs;
	}

	@Override
	public int compareTo(FD o) {
		return o.lhsSort.compareTo(lhsSort);
	}

	@Override
	public String toString() {
		return lhs + "->" + rhs;
	}

	public void setSort(Integer[] indexes) {
		lhsSort = LongBitSet.FACTORY.create();
		for (Integer i : indexes) {
			if (lhs.get(indexes[i])) {
				lhsSort.set(i);
			}
		}
	}
}
