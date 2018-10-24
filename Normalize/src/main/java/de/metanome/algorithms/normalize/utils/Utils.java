package de.metanome.algorithms.normalize.utils;

import java.util.BitSet;

public class Utils {
	
	public static int andNotCount(BitSet base, BitSet not) {
		BitSet andNotSet = (BitSet) base.clone();
		andNotSet.andNot(not);
		return andNotSet.cardinality();
	}
	
	public static int intersectionCount(BitSet set1, BitSet set2) {
		BitSet intersection = (BitSet) set1.clone();
		intersection.and(set2);
		return intersection.cardinality();
	}
}
