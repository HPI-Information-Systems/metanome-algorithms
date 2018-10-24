package de.metanome.algorithms.normalize.structures;

import java.util.BitSet;

public class LhsTree extends LhsNode {

	protected int numAttributes;
	
	public LhsTree(int numAttributes) {
		super(numAttributes);
		this.numAttributes = numAttributes;
	}
	
	public void add(BitSet lhs) {
		// If this node is already a leaf, we do not need any children for the subset checks
		if (this.isLeaf)
			return;
		
		int childAttribute = lhs.nextSetBit(0);
		
		if (childAttribute < 0) {
			this.isLeaf = true;
			return;
		}
			
		if (this.children[childAttribute] == null)
			this.children[childAttribute] = new LhsNode(this.numAttributes, lhs.nextSetBit(childAttribute + 1), lhs);
		else
			this.children[childAttribute].add(this.numAttributes, lhs.nextSetBit(childAttribute + 1), lhs);
	}
	
	public boolean containsLhsOrSubset(BitSet lhs) {
		if (this.isLeaf)
			return true;
		
		for (int childAttribute = lhs.nextSetBit(0); childAttribute >= 0; childAttribute = lhs.nextSetBit(childAttribute + 1))
			if ((this.children[childAttribute] != null) && this.children[childAttribute].containsLhsOrSubset(lhs.nextSetBit(childAttribute + 1), lhs))
				return true;
		return false;
	}
}
