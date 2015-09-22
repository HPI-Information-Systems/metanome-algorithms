package de.metanome.algorithms.aidfd.helpers;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;

public class Cluster {
	private TIntArrayList array;
	private int column;

	public Cluster(int column) {
		this.column = column;
		this.array = new TIntArrayList();
	}

	public int getColumn() {
		return column;
	}
	
	public TIntArrayList getArray() {
		return array;
	}
	

	public void add(int value) {
		this.array.add(value);
	}

	public boolean contains(int value) {
		return array.binarySearch(value) >= 0;
	}

	public TIntIterator iterator() {
		return array.iterator();
	}

	public int size() {
		return array.size();
	}

	public int get(int index) {
		return array.get(index);
	}
}
