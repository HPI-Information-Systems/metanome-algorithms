package de.metanome.algorithms.cfdfinder.structures;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class LhsTrie extends LhsTrieElement {

	protected int numAttributes;
	
	public LhsTrie(int numAttributes) {
		this.numAttributes = numAttributes;
	}

	public LhsTrieElement addLhs(BitSet lhs) {
		LhsTrieElement currentNode = this;
		for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
			if (currentNode.getChildren()[i] != null)
				currentNode.setChild(this.numAttributes, i, new LhsTrieElement());
			currentNode = currentNode.getChildren()[i];
		}
		return currentNode;
	}

	public void removeLhs(BitSet lhs) {
		LhsTrieElement[] path = new LhsTrieElement[lhs.cardinality()];
		int currentPathIndex = 0;
		
		LhsTrieElement currentNode = this;
		path[currentPathIndex] = currentNode;
		currentPathIndex++;
		
		for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
			currentNode = currentNode.getChildren()[i];
			path[currentPathIndex] = currentNode;
			currentPathIndex++;
		}
		
		for (int i = path.length - 1; i >= 0; i --) {
			path[i].removeChild(i);
			if (path[i].getChildren() != null)
				break;
		}
	}
	
	public List<BitSet> getLhsAndGeneralizations(BitSet lhs) {
		List<BitSet> foundLhs = new ArrayList<>();
		BitSet currentLhs = new BitSet();
		int nextLhsAttr = lhs.nextSetBit(0);
		this.getLhsAndGeneralizations(lhs, nextLhsAttr, currentLhs, foundLhs);
		return foundLhs;
	}

	public boolean containsLhsOrGeneralization(BitSet lhs) {
		int nextLhsAttr = lhs.nextSetBit(0);
		return this.containsLhsOrGeneralization(lhs, nextLhsAttr);
	}

	public List<BitSet> asBitSetList() {
		List<BitSet> foundLhs = new ArrayList<>();
		BitSet currentLhs = new BitSet();
		int nextLhsAttr = 0;
		this.asBitSetList(currentLhs, nextLhsAttr, foundLhs);
		return foundLhs;
	}
}
