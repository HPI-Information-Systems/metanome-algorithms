package de.metanome.algorithms.cfdfinder.structures;

import java.util.BitSet;
import java.util.List;

public class LhsTrieElement {

	protected LhsTrieElement[] children = null;
	protected int childrenCount = 0;
	
	public LhsTrieElement[] getChildren() {
		return children;
	}
	
	public void setChild(int numAttributes, int index, LhsTrieElement child) {
		if (this.children == null)
			this.children = new LhsTrieElement[numAttributes];
		
		this.children[index] = child;
		this.childrenCount++;
	}

	public void removeChild(int index) {
		this.children[index] = null;
		this.childrenCount--;
		
		if (this.childrenCount == 0)
			this.children = null;
	}
	
	public int getChildrenCount() {
		return childrenCount;
	}
	
	public void setChildrenCount(int childrenCount) {
		this.childrenCount = childrenCount;
	}

	protected void getLhsAndGeneralizations(BitSet lhs, int currentLhsAttr, BitSet currentLhs, List<BitSet> foundLhs) {
		if (this.children == null) {
			foundLhs.add((BitSet) currentLhs.clone());
			return;
		}
		
		while (currentLhsAttr >= 0) {
			int nextLhsAttr = lhs.nextSetBit(currentLhsAttr + 1);
			
			if (this.children[currentLhsAttr] != null) {
				currentLhs.set(currentLhsAttr);
				this.children[currentLhsAttr].getLhsAndGeneralizations(lhs, nextLhsAttr, currentLhs, foundLhs);
				currentLhs.clear(currentLhsAttr);
			}
			
			currentLhsAttr = nextLhsAttr;
		}
	}

	protected boolean containsLhsOrGeneralization(BitSet lhs, int currentLhsAttr) {
		if (this.children == null)
			return true;
		
		// Is the dependency already read and we have not yet found a generalization?
		if (currentLhsAttr < 0)
			return false;
		
		int nextLhsAttr = lhs.nextSetBit(currentLhsAttr + 1);
		
		if (this.children[currentLhsAttr] != null)
			if (this.children[currentLhsAttr].containsLhsOrGeneralization(lhs, nextLhsAttr))
				return true;
		
		return this.containsLhsOrGeneralization(lhs, nextLhsAttr);
	}

	protected void asBitSetList(BitSet currentLhs, int currentLhsAttr, List<BitSet> foundLhs) {
		if (this.children == null) {
			foundLhs.add((BitSet) currentLhs.clone());
			return;
		}
		
		for (int lhsAttr = currentLhsAttr; lhsAttr < this.children.length; lhsAttr++) {
			if (this.children[lhsAttr] != null) {
				currentLhs.set(lhsAttr);
				this.children[lhsAttr].asBitSetList(currentLhs, lhsAttr + 1, foundLhs);
				currentLhs.clear(lhsAttr);
			}
		}
	}
	
}
