package de.hpi.naumann.dc.evidenceset.build.sampling;

import java.util.Random;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;

class OrderedCluster {
	private final TIntArrayList array;
	
	public OrderedCluster() {
		this.array = new TIntArrayList();
	}
	
	public void randomize() {
		for (int i=array.size(); i>1; i--)
            swap(array, i-1, r.nextInt(i));
	}
	
	public void add(int i) {
		array.add(i);
	}
	
	private void swap(TIntArrayList l, int i, int j) {
		l.set(i, l.set(j, l.get(i)));
	}

	private static Random r = new Random();

	public int size() {
		return array.size();
	}

	public int nextLine() {
//		return array.get(next++ % size());
		return array.get(r.nextInt(size()));
	}

	public TIntIterator iterator() {
		return array.iterator();
	}

	
}
