package de.hpi.is.md.hybrid.impl.lattice.md;

import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.md.MDSite;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LevelRetriever {

	private final MDSite lhs;
	private final int level;
	private final LevelFunction levelFunction;
	private final Collection<LhsRhsPair> results = new ArrayList<>();

	static LevelRetriever create(LevelFunction levelFunction, int level) {
		int size = levelFunction.size();
		MDSite lhs = new MDSiteImpl(size);
		return new LevelRetriever(lhs, level, levelFunction);
	}

	Collection<LhsRhsPair> get(Node root) {
		get(root, 0);
		return results;
	}

	private void get(Node root, int currentLevel) {
		with(root, currentLevel).get();
	}

	private WithThresholdNode with(int lhsAttr, ThresholdNode t) {
		return new WithThresholdNode(t, lhsAttr);
	}

	private WithNode with(Node node, int currentLevel) {
		return new WithNode(node, currentLevel);
	}

	@RequiredArgsConstructor
	private class WithNode {

		private final Node node;
		private final int currentLevel;

		private void get() {
			if (currentLevel == level) {
				if (node.getRhs().isNotEmpty()) {
					LhsRhsPair pair = toPair();
					results.add(pair);
				}
			} else {
				node.getChildren().forEach(this::getLevelInChild);
			}
		}

		private void getLevelInChild(int lhsAttr, ThresholdNode t) {
			with(lhsAttr, t).getLevelForEachChild(currentLevel);
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

		private Consumer<Node> getLevel(int nextLevel) {
			return node -> get(node, nextLevel);
		}

		private void getLevelForEachChild(int currentLevel) {
			for (Entry<Node> entry : thresholdNode.getChildren().double2ObjectEntrySet()) {
				double threshold = entry.getDoubleKey();
				int nextLevel = currentLevel + getSingleDistance(threshold);
				if (nextLevel > level) {
					return;
				}
				apply(getLevel(nextLevel), entry);
			}
		}

		private int getSingleDistance(double threshold) {
			return levelFunction.getSingleDistance(lhsAttr, threshold);
		}
	}
}
