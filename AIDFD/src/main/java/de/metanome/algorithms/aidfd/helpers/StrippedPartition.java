package de.metanome.algorithms.aidfd.helpers;

import java.util.ArrayList;
import java.util.List;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;

public class StrippedPartition {
	public IBitSet currentBS;
	private List<Partition> partitions;
	public static ArrayList<Cluster[]> clusters;
	public static List<Partition>[] columns;

	protected StrippedPartition(IBitSet bs, List<Partition> ancestorParts) {
		this.currentBS = bs;
		this.partitions = ancestorParts;
	}

	public static StrippedPartition EMPTY = new StrippedPartition(
			LongBitSet.FACTORY.create(), null) {
		public StrippedPartition getNew(IBitSet newIntersect) {
			int firstColumn = newIntersect.nextSetBit(0);
			if (firstColumn < 0)
				return this;
			StrippedPartition newSP = new StrippedPartition(newIntersect, columns[firstColumn]);
			for (int i = newIntersect.nextSetBit(firstColumn + 1); i >= 0; i = newIntersect
					.nextSetBit(i + 1)) {
				newSP.refine(i);
			}
			return newSP;
		}

		public boolean isRefindedBy(int target) {
			// problematic? need to check partition size as well?
			return columns[target].size() == 1;
		};
	};

	public StrippedPartition getNew(IBitSet newIntersect) {
		if (newIntersect.equals(currentBS))
			return this;

		StrippedPartition newSP = new StrippedPartition(newIntersect,
				this.partitions);
		for (int i = newIntersect.nextSetBit(0); i >= 0; i = newIntersect
				.nextSetBit(i + 1)) {
			if (!currentBS.get(i))
				newSP.refine(i);
		}
		return newSP;
	}

	private void refine(int i) {
		List<Partition> oldPartitions = partitions;
		this.partitions = new ArrayList<Partition>();
		for (Partition oldP : oldPartitions) {
			for (Partition p : oldP.refineBy(i)) {
				if (p.size() > 1)
					this.partitions.add(p);
			}
		}
	}

	public boolean isRefindedBy(int target) {
		for (Partition c : partitions) {
			if (c.isRefinedBy(target)) {
				return true;
			}
		}
		return false;
	}
}
