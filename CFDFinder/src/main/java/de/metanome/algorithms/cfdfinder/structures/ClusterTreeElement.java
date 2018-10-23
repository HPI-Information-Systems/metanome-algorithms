package de.metanome.algorithms.cfdfinder.structures;

import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class ClusterTreeElement {

	protected Int2ObjectOpenHashMap<ClusterTreeElement> children = new Int2ObjectOpenHashMap<ClusterTreeElement>();
	protected int content = 0;
	
	public ClusterTreeElement(int[][] compressedRecords, BitSet lhs, int nextLhsAttr, int recordId, int content) {
		if (nextLhsAttr < 0) {
			this.content = content;
		}
		else {
			int nextCluster = compressedRecords[recordId][nextLhsAttr];
			if (nextCluster < 0)
				return;
			
			ClusterTreeElement child = new ClusterTreeElement(compressedRecords, lhs, lhs.nextSetBit(nextLhsAttr + 1), recordId, content);
			this.children.put(nextCluster, child);
		}
	}
	
	public boolean add(int[][] compressedRecords, BitSet lhs, int nextLhsAttr, int recordId, int content) {
		if (nextLhsAttr < 0)
			return this.content != -1 && this.content == content;
		
		int nextCluster = compressedRecords[recordId][nextLhsAttr];
		if (nextCluster < 0)
			return true;
		
		ClusterTreeElement child = this.children.get(nextCluster);
		if (child == null) {
			child = new ClusterTreeElement(compressedRecords, lhs, lhs.nextSetBit(nextLhsAttr + 1), recordId, content);
			this.children.put(nextCluster, child);
			return true;
		}
		
		return child.add(compressedRecords, lhs, lhs.nextSetBit(nextLhsAttr + 1), recordId, content);
	}
}
