package de.metanome.algorithms.hyucc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.lucene.util.OpenBitSet;

import de.metanome.algorithms.hyucc.structures.IntegerPair;
import de.metanome.algorithms.hyucc.structures.PositionListIndex;
import de.metanome.algorithms.hyucc.structures.UCCList;
import de.metanome.algorithms.hyucc.structures.UCCSet;
import de.metanome.algorithms.hyucc.structures.UCCTree;
import de.metanome.algorithms.hyucc.utils.Logger;
import de.metanome.algorithms.hyucc.utils.ValueComparator;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class Sampler {

	private UCCSet negCover;
	private UCCTree posCover;
	private int[][] compressedRecords;
	private List<PositionListIndex> plis;
	private float efficiencyThreshold;
	private ValueComparator valueComparator;
	private List<AttributeRepresentant> attributeRepresentants = null;
	private PriorityQueue<AttributeRepresentant> queue = null;
	private MemoryGuardian memoryGuardian;

	public Sampler(UCCSet negCover, UCCTree posCover, int[][] compressedRecords, List<PositionListIndex> plis, float efficiencyThreshold, ValueComparator valueComparator, MemoryGuardian memoryGuardian) {
		this.negCover = negCover;
		this.posCover = posCover;
		this.compressedRecords = compressedRecords;
		this.plis = plis;
		this.efficiencyThreshold = efficiencyThreshold;
		this.valueComparator = valueComparator;
		this.memoryGuardian = memoryGuardian;
	}

	public UCCList enrichNegativeCover(List<IntegerPair> comparisonSuggestions) {
		int numAttributes = this.compressedRecords[0].length;
		
		Logger.getInstance().writeln("Investigating comparison suggestions ... ");
		UCCList newNonUCCs = new UCCList(numAttributes, this.negCover.getMaxDepth());
		OpenBitSet equalAttrs = new OpenBitSet(this.posCover.getNumAttributes());
		for (IntegerPair comparisonSuggestion : comparisonSuggestions) {
			this.match(equalAttrs, comparisonSuggestion.a(), comparisonSuggestion.b());
			
			if (!this.negCover.contains(equalAttrs)) {
				OpenBitSet equalAttrsCopy = equalAttrs.clone();
				this.negCover.add(equalAttrsCopy);
				newNonUCCs.add(equalAttrsCopy);
				
				this.memoryGuardian.memoryChanged(1);
				this.memoryGuardian.match(this.negCover, this.posCover, newNonUCCs);
			}
		}
		
		if (this.attributeRepresentants == null) { // if this is the first call of this method
			Logger.getInstance().write("Sorting clusters ...");
			long time = System.currentTimeMillis();
			ClusterComparator comparator = new ClusterComparator(this.compressedRecords, this.compressedRecords[0].length - 1, 1);
			for (PositionListIndex pli : this.plis) {
				for (IntArrayList cluster : pli.getClusters()) {
					Collections.sort(cluster, comparator);
				}
				comparator.incrementActiveKey();
			}
			Logger.getInstance().writeln("(" + (System.currentTimeMillis() - time) + "ms)");
		
			Logger.getInstance().write("Running initial windows ...");
			time = System.currentTimeMillis();
			this.attributeRepresentants = new ArrayList<AttributeRepresentant>(numAttributes);
			this.queue = new PriorityQueue<AttributeRepresentant>(numAttributes);
			for (int i = 0; i < numAttributes; i++) {
				AttributeRepresentant attributeRepresentant = new AttributeRepresentant(this.plis.get(i).getClusters(), this.negCover, this.posCover, this, this.memoryGuardian);
				attributeRepresentant.runNext(newNonUCCs, this.compressedRecords);
				this.attributeRepresentants.add(attributeRepresentant);
				if (attributeRepresentant.getEfficiency() > 0.0f)
					this.queue.add(attributeRepresentant); // If the efficiency is 0, the algorithm will never schedule a next run for the attribute regardless how low we set the efficiency threshold
			}

			if (!this.queue.isEmpty())
				this.efficiencyThreshold = Math.min(0.01f, this.queue.peek().getEfficiency() * 0.5f); // This is an optimization that we added after writing the HyFD paper
			
			Logger.getInstance().writeln("(" + (System.currentTimeMillis() - time) + "ms)");
		}
		else {
			// Decrease the efficiency threshold
			if (!this.queue.isEmpty())
				this.efficiencyThreshold = Math.min(this.efficiencyThreshold / 2, this.queue.peek().getEfficiency() * 0.9f); // This is an optimization that we added after writing the HyFD paper
		}
		
		Logger.getInstance().writeln("Moving window over clusters ... ");
		
		while (!this.queue.isEmpty() && (this.queue.peek().getEfficiency() >= this.efficiencyThreshold)) {
			AttributeRepresentant attributeRepresentant = this.queue.remove();
			
			attributeRepresentant.runNext(newNonUCCs, this.compressedRecords);
			
			if (attributeRepresentant.getEfficiency() > 0.0f)
				this.queue.add(attributeRepresentant);
		}
		
		StringBuilder windows = new StringBuilder("Window signature: ");
		for (AttributeRepresentant attributeRepresentant : this.attributeRepresentants)
			windows.append("[" + attributeRepresentant.windowDistance + "]");
		
		return newNonUCCs;
	}

	private class ClusterComparator implements Comparator<Integer> {
		
		private int[][] sortKeys;
		private int activeKey1;
		private int activeKey2;
		
		public ClusterComparator(int[][] sortKeys, int activeKey1, int activeKey2) {
			super();
			this.sortKeys = sortKeys;
			this.activeKey1 = activeKey1;
			this.activeKey2 = activeKey2;
		}
		
		public void incrementActiveKey() {
			this.activeKey1 = this.increment(this.activeKey1);
			this.activeKey2 = this.increment(this.activeKey2);
		}
		
		@Override
		public int compare(Integer o1, Integer o2) {
			// Previous -> Next
			int value1 = this.sortKeys[o1.intValue()][this.activeKey1];
			int value2 = this.sortKeys[o2.intValue()][this.activeKey1];
			int result = value2 - value1;
			if (result == 0) {
				value1 = this.sortKeys[o1.intValue()][this.activeKey2];
				value2 = this.sortKeys[o2.intValue()][this.activeKey2];
			}
			return value2 - value1;
		}
		
		private int increment(int number) {
			return (number == this.sortKeys[0].length - 1) ? 0 : number + 1;
		}
	}

	private class AttributeRepresentant implements Comparable<AttributeRepresentant> {
		
		private int windowDistance = 0;
		private IntArrayList numNewNonFds = new IntArrayList();
		private IntArrayList numComparisons = new IntArrayList();
		private List<IntArrayList> clusters;
		private UCCSet negCover;
		private UCCTree posCover;
		private Sampler sampler;
		private MemoryGuardian memoryGuardian;
		
		public float getEfficiency() {
			int index = this.numNewNonFds.size() - 1;
			float sumNewNonFds = this.numNewNonFds.getInt(index);
			float sumComparisons = this.numComparisons.getInt(index);
			if (sumComparisons == 0)
				return 0.0f;
			return sumNewNonFds / sumComparisons;
		}
		
		public AttributeRepresentant(List<IntArrayList> clusters, UCCSet negCover, UCCTree posCover, Sampler sampler, MemoryGuardian memoryGuardian) {
			this.clusters = new ArrayList<IntArrayList>(clusters);
			this.negCover = negCover;
			this.posCover = posCover;
			this.sampler = sampler;
			this.memoryGuardian =  memoryGuardian;
		}
		
		@Override
		public int compareTo(AttributeRepresentant o) {		
			return (int)Math.signum(o.getEfficiency() - this.getEfficiency());
		}
		
		public void runNext(UCCList newNonUCCs, int[][] compressedRecords) {
			this.windowDistance++;
			int numNewNonFds = 0;
			int numComparisons = 0;
			OpenBitSet equalAttrs = new OpenBitSet(this.posCover.getNumAttributes());
			
			int previousNewNonFdsSize = newNonUCCs.size();
			Iterator<IntArrayList> clusterIterator = this.clusters.iterator();
			while (clusterIterator.hasNext()) {
				IntArrayList cluster = clusterIterator.next();
				
				if (cluster.size() <= this.windowDistance) {
					clusterIterator.remove();
					continue;
				}
				
				for (int recordIndex = 0; recordIndex < (cluster.size() - this.windowDistance); recordIndex++) {
					int recordId = cluster.getInt(recordIndex);
					int partnerRecordId = cluster.getInt(recordIndex + this.windowDistance);
					
					this.sampler.match(equalAttrs, compressedRecords[recordId], compressedRecords[partnerRecordId]);
					
					if (!this.negCover.contains(equalAttrs)) {
						OpenBitSet equalAttrsCopy = equalAttrs.clone();
						this.negCover.add(equalAttrsCopy);
						newNonUCCs.add(equalAttrsCopy);
						
						this.memoryGuardian.memoryChanged(1);
						this.memoryGuardian.match(this.negCover, this.posCover, newNonUCCs);
					}
					numComparisons++;
				}
			}
			numNewNonFds = newNonUCCs.size() - previousNewNonFdsSize;
			
			this.numNewNonFds.add(numNewNonFds);
			this.numComparisons.add(numComparisons);
		}
	}
	
	private void match(OpenBitSet equalAttrs, int t1, int t2) {
		this.match(equalAttrs, this.compressedRecords[t1], this.compressedRecords[t2]);
	}
	
	private void match(OpenBitSet equalAttrs, int[] t1, int[] t2) {
		equalAttrs.clear(0, t1.length);
		for (int i = 0; i < t1.length; i++)
			if (this.valueComparator.isEqual(t1[i], t2[i]))
				equalAttrs.set(i);
	}
	
}
