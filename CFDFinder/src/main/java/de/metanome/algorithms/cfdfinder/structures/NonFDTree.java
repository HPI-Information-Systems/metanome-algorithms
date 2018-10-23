package de.metanome.algorithms.cfdfinder.structures;

import java.util.ArrayList;
import java.util.BitSet;

import de.metanome.algorithms.cfdfinder.utils.ValueComparator;

public class NonFDTree extends NonFDTreeElement {

	int size = 0;
	
	public int size() {
		return this.size;
	}

	public NonFDTree(int numAttributes) {
		super(numAttributes);
	}

	public boolean addMatches(int[] t1, int[] t2, ValueComparator valueComparator) {
		int attribute = 0;
		boolean newNonFD = false;
		
		while (valueComparator.isDifferent(t1[attribute], t2[attribute])) {
			attribute++;
			if (attribute == t1.length)
				return newNonFD;
		}
		
		if (this.children[attribute] == null) {
			this.children[attribute] = new NonFDTreeElement(this.children.length);
			newNonFD = true;
		}
		
		newNonFD = this.children[attribute].addMatches(t1, t2, valueComparator, attribute, newNonFD);
		if (newNonFD)
			this.size++;
		
		return newNonFD;
	}

	public ArrayList<BitSet> asBitSets() {
		ArrayList<BitSet> bitsets = new ArrayList<>(this.size);
		BitSet bitset = new BitSet(this.children.length);
		
		for (int i = 0; i < this.children.length; i++)
			if (this.children[i] != null)
				this.children[i].asBitSets(bitsets, bitset, i);
		
		return bitsets;
	}
}
