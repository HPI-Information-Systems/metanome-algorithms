package de.hpi.is.md.hybrid.impl.lattice.candidate;

import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.impl.level.Candidate;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap.Entry;
import it.unimi.dsi.fastutil.doubles.Double2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import java.util.List;

class CandidateThresholdNode {

	private final Double2ObjectSortedMap<CandidateNode> children = new Double2ObjectRBTreeMap<>();

	private static CandidateNode createChild(int columnPairs) {
		return new CandidateNode(columnPairs);
	}

	void add(LatticeMD latticeMD, MDElement rhsElem, int nextLhsAttr, double threshold) {
		MDSite lhs = latticeMD.getLhs();
		int columnPairs = lhs.size();
		CandidateNode node = getOrCreateChild(threshold, columnPairs);
		node.add(latticeMD, rhsElem, nextLhsAttr);
	}

	boolean containsMdOrGeneralization(MDSite lhs, int rhsAttr, int nextLhsAttr, double max) {
		for (Entry<CandidateNode> entry : children.double2ObjectEntrySet()) {
			double threshold = entry.getDoubleKey();
			if (threshold > max) {
				return false;
			}
			CandidateNode node = entry.getValue();
			if (node.containsMdOrGeneralization(lhs, rhsAttr, nextLhsAttr)) {
				return true;
			}
		}
		return false;
	}

	void getAll(List<Candidate> results) {
		for (Entry<CandidateNode> entry : children.double2ObjectEntrySet()) {
			CandidateNode node = entry.getValue();
			node.getAll(results);
		}
	}

	boolean remove(MDSite lhs, int rhsAttr, int nextLhsAttr, double min, boolean specialized) {
		for (Entry<CandidateNode> entry : children.double2ObjectEntrySet()) {
			double threshold = entry.getDoubleKey();
			if (threshold < min) {
				continue;
			}
			if (threshold > min) {
				specialized = true;
			}
			CandidateNode node = entry.getValue();
			node.removeSpecializations(lhs, rhsAttr, nextLhsAttr, specialized);
		}
		return false;
	}

	private CandidateNode getOrCreateChild(double threshold, int columnPairs) {
		return children.computeIfAbsent(threshold, __ -> createChild(columnPairs));
	}
}
