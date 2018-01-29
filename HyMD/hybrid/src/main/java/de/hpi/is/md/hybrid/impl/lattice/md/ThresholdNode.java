package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.MathUtils;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap.Entry;
import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ThresholdNode {

	@Getter
	private final Double2ObjectSortedMap<Node> children = new Double2ObjectRBTreeMap<>();

	private static Node createChild(int size) {
		return new Node(size);
	}

	LhsRhsPair add(MD md, int nextLhsAttr, double threshold) {
		Node node = getOrCreateChild(threshold, md);
		return node.add(md, nextLhsAttr);
	}

	Optional<LhsRhsPair> addIfMinimal(MD md, int nextLhsAttr, double threshold) {
		for (Entry<Node> entry : children.double2ObjectEntrySet()) {
			double t = entry.getDoubleKey();
			if (t > threshold) {
				break;
			}
			Node node = entry.getValue();
			if (t == threshold) {
				return node.addIfMinimal(md, nextLhsAttr);
			}
			if (node.containsMdOrGeneralization(md, nextLhsAttr)) {
				return Optional.empty();
			}
		}
		Node node = getOrCreateChild(threshold, md);
		return node.addIfMinimal(md, nextLhsAttr);
	}

	boolean containsMdOrGeneralization(MD md, int nextLhsAttr, double max) {
		for (Entry<Node> entry : children.double2ObjectEntrySet()) {
			double threshold = entry.getDoubleKey();
			if (threshold > max) {
				return false;
			}
			Node node = entry.getValue();
			if (node.containsMdOrGeneralization(md, nextLhsAttr)) {
				return true;
			}
		}
		return false;
	}

	double[] getMaxThreshold(MDSite lhs, int[] rhsAttrs, int nextLhsAttr, double max) {
		double[] maxThreshold = new double[rhsAttrs.length];
		for (Entry<Node> entry : children.double2ObjectEntrySet()) {
			double threshold = entry.getDoubleKey();
			if (threshold > max) {
				return maxThreshold;
			}
			Node node = entry.getValue();
			double[] nodeMaxThreshold = node.getMaxThreshold(lhs, rhsAttrs, nextLhsAttr);
			maxThreshold = MathUtils.max(maxThreshold, nodeMaxThreshold);
			if (MaxContext.allMax(maxThreshold)) {
				return maxThreshold;
			}
		}
		return maxThreshold;
	}

	private Node getOrCreateChild(double threshold, MD md) {
		MDSite lhs = md.getLhs();
		int size = lhs.size();
		return getOrCreateChild(threshold, size);
	}

	private Node getOrCreateChild(double threshold, int size) {
		return children.computeIfAbsent(threshold, __ -> createChild(size));
	}
}
