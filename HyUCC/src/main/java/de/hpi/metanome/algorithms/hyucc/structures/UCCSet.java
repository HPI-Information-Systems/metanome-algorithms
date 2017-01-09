package de.hpi.metanome.algorithms.hyucc.structures;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class UCCSet {

	private List<ObjectOpenHashSet<OpenBitSet>> uccLevels;
	
	private int depth = 0;
	private int maxDepth;
	
	public UCCSet(int numAttributes, int maxDepth) {
		this.maxDepth = maxDepth;
		this.uccLevels = new ArrayList<ObjectOpenHashSet<OpenBitSet>>(numAttributes);
		for (int i = 0; i <= numAttributes; i++)
			this.uccLevels.add(new ObjectOpenHashSet<OpenBitSet>());
	}
	
	public List<ObjectOpenHashSet<OpenBitSet>> getUccLevels() {
		return this.uccLevels;
	}

	public int getDepth() {
		return this.depth;
	}

	public int getMaxDepth() {
		return this.maxDepth;
	}

	public boolean add(OpenBitSet ucc) {
		int length = (int) ucc.cardinality();
		
		if ((this.maxDepth > 0) && (length > this.maxDepth))
			return false;
		
		this.depth = Math.max(this.depth, length);
		return this.uccLevels.get(length).add(ucc);
	}
	
	public boolean contains(OpenBitSet ucc) {
		int length = (int) ucc.cardinality();
		
		if ((this.maxDepth > 0) && (length > this.maxDepth))
			return false;
		
		return this.uccLevels.get(length).contains(ucc);
	}
	
	public void trim(int newDepth) {
		while (this.uccLevels.size() > (newDepth + 1)) // +1 because uccLevels contains level 0
			this.uccLevels.remove(this.uccLevels.size() - 1);
		
		this.depth = newDepth;
		this.maxDepth = newDepth;
	}

	public void clear() {
		int numLevels = this.uccLevels.size();
		this.uccLevels = new ArrayList<ObjectOpenHashSet<OpenBitSet>>(numLevels);
		for (int i = 0; i <= numLevels; i++)
			this.uccLevels.add(new ObjectOpenHashSet<OpenBitSet>());
	}

	public int size() {
		int size = 0;
		for (ObjectOpenHashSet<OpenBitSet> uccs : this.uccLevels)
			size += uccs.size();
		return size;
	}
}
