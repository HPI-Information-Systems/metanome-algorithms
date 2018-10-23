package de.metanome.algorithms.cfdfinder.structures;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class FDSet {

	private List<ObjectOpenHashSet<BitSet>> fdLevels;
	
	private int depth = 0;
	private int maxDepth;
	
	public FDSet(int numAttributes, int maxDepth) {
		this.maxDepth = maxDepth;
		this.fdLevels = new ArrayList<ObjectOpenHashSet<BitSet>>(numAttributes);
		for (int i = 0; i <= numAttributes; i++)
			this.fdLevels.add(new ObjectOpenHashSet<BitSet>());
	}

	public List<ObjectOpenHashSet<BitSet>> getFdLevels() {
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

	public boolean contains(BitSet fd) {
		int length = fd.cardinality();
		
		if ((this.maxDepth > 0) && (length > this.maxDepth))
			return false;
		
		return this.fdLevels.get(length).contains(fd);
	}
	
	public void trim(int newDepth) {
		while (this.fdLevels.size() > (newDepth + 1)) // +1 because uccLevels contains level 0
			this.fdLevels.remove(this.fdLevels.size() - 1);
		
		this.depth = newDepth;
		this.maxDepth = newDepth;
	}

	public void clear() {
		int numLevels = this.fdLevels.size();
		this.fdLevels = new ArrayList<ObjectOpenHashSet<BitSet>>(numLevels);
		for (int i = 0; i <= numLevels; i++)
			this.fdLevels.add(new ObjectOpenHashSet<BitSet>());
	}

	public int size() {
		int size = 0;
		for (ObjectOpenHashSet<BitSet> uccs : this.fdLevels)
			size += uccs.size();
		return size;
	}
}
