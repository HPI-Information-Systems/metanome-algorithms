package de.hpi.is.md.hybrid.impl.lattice.lhs;

import de.hpi.is.md.hybrid.md.MDSite;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap.Entry;
import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;

class LhsThresholdNode {

	private final Double2ObjectSortedMap<LhsNode> children = new Double2ObjectRBTreeMap<>();

	private static LhsNode createChild(int columnPairs) {
		return new LhsNode(columnPairs);
	}

	void add(MDSite lhs, int nextLhsAttr, double threshold) {
		int columnPairs = lhs.size();
		LhsNode node = getOrCreateChild(threshold, columnPairs);
		node.add(lhs, nextLhsAttr);
	}

	boolean containsMdOrGeneralization(MDSite lhs, int nextLhsAttr, double max) {
		for (Entry<LhsNode> entry : children.double2ObjectEntrySet()) {
			double threshold = entry.getDoubleKey();
			if (threshold > max) {
				return false;
			}
			LhsNode node = entry.getValue();
			if (node.containsMdOrGeneralization(lhs, nextLhsAttr)) {
				return true;
			}
		}
		return false;
	}

	private LhsNode getOrCreateChild(double threshold, int columnPairs) {
		return children.computeIfAbsent(threshold, __ -> createChild(columnPairs));
	}
}
