package de.metanome.algorithms.aidfd.helpers;

import java.util.Collection;
import java.util.HashMap;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;

public class Partition  {
	private TIntArrayList array;
	
	public Partition() {
		this.array = new TIntArrayList();
	}
	
	public Partition(Cluster c) {
		this();
		this.array.addAll(c.getArray());
	}

	public void add(int value) {
		this.array.add(value);
	}
	
	public int size() {
		return array.size();
	}

	public boolean isRefinedBy(int target) {
		Cluster first = StrippedPartition.clusters.get(array.get(0))[target];
		if(first == null || first.size() < this.size()) {
			return true;
		}
		for(TIntIterator iter = array.iterator(); iter.hasNext(); ) {
			int next = iter.next();
			if(StrippedPartition.clusters.get(next)[target] != first) {
				return true;
			}
		}
		return false;
	}

	public Collection<Partition> refineBy(int target) {
		HashMap<Cluster, Partition> map = new HashMap<Cluster, Partition>();
		for(TIntIterator iter = array.iterator(); iter.hasNext();) {
			int next = iter.next();
			Cluster c = StrippedPartition.clusters.get(next)[target];
			if(c == null) {
				continue;
			}
			if(map.containsKey(c)) {
				map.get(c).add(next);
			} else {
				Partition p = new Partition();
				p.add(next);
				map.put(c, p);
			}
		}
		return map.values();
	}
}
