package de.metanome.algorithms.cfdfinder.structures;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class FDList {

	private List<List<BitSet>> fdLevels;
	
	private int depth = 0;
	private int maxDepth;
	
	public FDList(int numAttributes, int maxDepth) {
		this.maxDepth = maxDepth;
		this.fdLevels = new ArrayList<List<BitSet>>(numAttributes);
		for (int i = 0; i <= numAttributes; i++)
			this.fdLevels.add(new ArrayList<BitSet>());
	}

	public List<List<BitSet>> getFdLevels() {
		return this.fdLevels;
	}

	public int getDepth() {
		return this.depth;
	}

	public int getMaxDepth() {
		return this.maxDepth;
	}

	public boolean add(BitSet fd) {
		int length = fd.cardinality();
		
		if ((this.maxDepth > 0) && (length > this.maxDepth))
			return false;
		
		this.depth = Math.max(this.depth, length);
		return this.fdLevels.get(length).add(fd);
	}

	public void trim(int newDepth) {
		while (this.fdLevels.size() > (newDepth + 1)) // +1 because uccLevels contains level 0
			this.fdLevels.remove(this.fdLevels.size() - 1);
		
		this.depth = newDepth;
		this.maxDepth = newDepth;
	}

	public void clear() {
		int numLevels = this.fdLevels.size();
		this.fdLevels = new ArrayList<List<BitSet>>(numLevels);
		for (int i = 0; i <= numLevels; i++)
			this.fdLevels.add(new ArrayList<BitSet>());
	}

	public int size() {
		int size = 0;
		for (List<BitSet> uccs : this.fdLevels)
			size += uccs.size();
		return size;
	}
}
