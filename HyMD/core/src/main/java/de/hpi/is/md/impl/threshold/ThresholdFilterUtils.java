package de.hpi.is.md.impl.threshold;

import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import it.unimi.dsi.fastutil.doubles.DoubleRBTreeSet;
import it.unimi.dsi.fastutil.doubles.DoubleSortedSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ThresholdFilterUtils {

	public static DoubleSortedSet sorted(DoubleCollection values) {
		//iterate backwards to ensure we retain the maximal threshold
		DoubleSortedSet filtered = new DoubleRBTreeSet(DoubleComparators.OPPOSITE_COMPARATOR);
		filtered.addAll(values);
		return filtered;
	}
}
