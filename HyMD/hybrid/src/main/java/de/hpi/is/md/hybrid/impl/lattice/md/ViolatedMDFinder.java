package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.hybrid.SimilaritySet;
import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.StreamUtils;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ViolatedMDFinder {

	private final MDSite lhs;
	private final SimilaritySet similaritySet;
	private final Collection<LhsRhsPair> results = new ArrayList<>();

	static ViolatedMDFinder create(SimilaritySet similaritySet) {
		int size = similaritySet.size();
		MDSite lhs = new MDSiteImpl(size);
		return new ViolatedMDFinder(lhs, similaritySet);
	}

	Collection<LhsRhsPair> find(Node root) {
		find_(root);
		return results;
	}

	private void find_(Node root) {
		with(root).find();
	}

	private WithThresholdNode with(int lhsAttr, ThresholdNode t) {
		return new WithThresholdNode(t, lhsAttr);
	}

	private WithNode with(Node node) {
		return new WithNode(node);
	}

	@RequiredArgsConstructor
	private class WithNode {

		private final Node node;

		private void find() {
			if (isViolatedHere()) {
				LhsRhsPair pair = toPair();
				results.add(pair);
			}
			node.getChildren().forEach(this::findViolatedInChild);
		}

		private void findViolatedInChild(int lhsAttr, ThresholdNode t) {
			with(lhsAttr, t).findViolated();
		}

		private boolean isViolatedHere() {
			return StreamUtils.seq(node.getRhs())
				.anyMatch(similaritySet::isViolated);
		}

		private LhsRhsPair toPair() {
			MDSite clone = lhs.clone();
			return new LhsRhsPair(clone, node.getRhs());
		}
	}

	@RequiredArgsConstructor
	private class WithThresholdNode {

		private final ThresholdNode thresholdNode;
		private final int lhsAttr;

		private void apply(Consumer<Node> action, Entry<Node> entry) {
			double threshold = entry.getDoubleKey();
			Node node = entry.getValue();
			lhs.set(lhsAttr, threshold);
			action.accept(node);
			lhs.clear(lhsAttr);
		}

		private void findViolated() {
			double max = similaritySet.get(lhsAttr);
			forEveryThreshold(ViolatedMDFinder.this::find_, max);
		}

		private void forEveryThreshold(Consumer<Node> action, double max) {
			for (Entry<Node> entry : thresholdNode.getChildren().double2ObjectEntrySet()) {
				double threshold = entry.getDoubleKey();
				if (threshold > max) {
					return;
				}
				apply(action, entry);
			}
		}
	}
}
