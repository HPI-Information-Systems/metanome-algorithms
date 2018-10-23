package de.metanome.algorithms.hyucc.structures;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class UCCList {

	private List<List<BitSet>> uccLevels;
	
	private int depth = 0;
	private int maxDepth;
	
	public UCCList(int numAttributes, int maxDepth) {
		this.maxDepth = maxDepth;
		this.uccLevels = new ArrayList<List<BitSet>>(numAttributes);
		for (int i = 0; i <= numAttributes; i++)
			this.uccLevels.add(new ArrayList<BitSet>());
	}
	
	public List<List<BitSet>> getUccLevels() {
		return this.uccLevels;
	}

	public int getDepth() {
		return this.depth;
	}

	public int getMaxDepth() {
		return this.maxDepth;
	}

	public boolean add(BitSet ucc) {
		int length = ucc.cardinality();
		
		if ((this.maxDepth > 0) && (length > this.maxDepth))
			return false;
		
		this.depth = Math.max(this.depth, length);
		return this.uccLevels.get(length).add(ucc);
	}
	
	public void trim(int newDepth) {
		while (this.uccLevels.size() > (newDepth + 1)) // +1 because uccLevels contains level 0
			this.uccLevels.remove(this.uccLevels.size() - 1);
		
		this.depth = newDepth;
		this.maxDepth = newDepth;
	}

	public void clear() {
		int numLevels = this.uccLevels.size();
		this.uccLevels = new ArrayList<List<BitSet>>(numLevels);
		for (int i = 0; i <= numLevels; i++)
			this.uccLevels.add(new ArrayList<BitSet>());
	}

	public int size() {
		int size = 0;
		for (List<BitSet> uccs : this.uccLevels)
			size += uccs.size();
		return size;
	}
}
