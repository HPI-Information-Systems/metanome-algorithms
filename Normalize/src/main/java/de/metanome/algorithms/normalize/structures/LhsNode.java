package de.metanome.algorithms.normalize.structures;

import java.util.BitSet;

public class LhsNode {

	protected boolean isLeaf;
	protected LhsNode[] children;
	
	public LhsNode(int numAttributes) {
		this.children = new LhsNode[numAttributes];
		this.isLeaf = false;
	}
	
	public LhsNode(int numAttributes, int childAttribute, BitSet lhs) {
		this(numAttributes);
		
		if (childAttribute < 0) {
			this.isLeaf = true;
			return;
		}
		
		if (this.children[childAttribute] == null)
			this.children[childAttribute] = new LhsNode(numAttributes, lhs.nextSetBit(childAttribute + 1), lhs);
		else
			this.children[childAttribute].add(numAttributes, lhs.nextSetBit(childAttribute + 1), lhs);
	}
	
	public void add(int numAttributes, int childAttribute, BitSet lhs) {
		// If this node is already a leaf, we do not need any children for the subset checks
		if (this.isLeaf)
			return;
		
		if (childAttribute < 0) {
			this.isLeaf = true;
			return;
		}
		
		if (this.children[childAttribute] == null)
			this.children[childAttribute] = new LhsNode(numAttributes, lhs.nextSetBit(childAttribute + 1), lhs);
		else
			this.children[childAttribute].add(numAttributes, lhs.nextSetBit(childAttribute + 1), lhs);
	}
	
	public boolean containsLhsOrSubset(int childAttribute, BitSet lhs) {
		if (this.isLeaf)
			return true;
		
		if (childAttribute < 0)
			return false;
		
		for (; childAttribute >= 0; childAttribute = lhs.nextSetBit(childAttribute + 1))
			if ((this.children[childAttribute] != null) && this.children[childAttribute].containsLhsOrSubset(lhs.nextSetBit(childAttribute + 1), lhs))
				return true;
		return false;
	}
}
