package fdiscovery.partitions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import fdiscovery.columns.ColumnCollection;
//import fdiscovery.pruning.PartitionEquivalences;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class MemoryManagedJoinedPartitions extends TLongObjectHashMap<HashMap<ColumnCollection, Partition>> {

	private static final long serialVersionUID = -7385828030861564827L;
	private static final int PARTITION_THRESHOLD = 10000;
	private static final boolean USE_MEMORY_MANAGEMENT = false;
	
	private int numberOfColumns;
	private ColumnCollection key;
	private TObjectIntHashMap<ColumnCollection> usageCounter;
	private LinkedList<ColumnCollection> leastRecentlyUsedPartitions;
	private TObjectIntHashMap<ColumnCollection> totalCount;
	
	public MemoryManagedJoinedPartitions(int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
		this.key = new ColumnCollection(numberOfColumns);
		if (USE_MEMORY_MANAGEMENT) {
			this.usageCounter = new TObjectIntHashMap<>();
			this.leastRecentlyUsedPartitions = new LinkedList<ColumnCollection>();
			this.totalCount = new TObjectIntHashMap<>();
		}
		for (long cardinality = 1; cardinality <= this.numberOfColumns; cardinality++) {
			this.put(cardinality, new HashMap<ColumnCollection, Partition>());
		}
	}

	public int getTotalCount() {
		int totalCount = 0;
		for (ColumnCollection key : this.totalCount.keySet()) {
			totalCount += this.totalCount.get(key);
		}
		
		return totalCount;
	}
	
	public int getUniqueCount() {
		return this.totalCount.size();
	}
	
	public void reset() {
		for (long cardinality = 2; cardinality <= this.numberOfColumns; cardinality++) {
			this.get(cardinality).clear();
		}
	}
	 
	public int getCount() {
		int cumulatedCount = 0;
		for (HashMap<ColumnCollection, Partition> elementsOfLevel : this.valueCollection()) {
			cumulatedCount += elementsOfLevel.size();
		}
		return cumulatedCount;
	}
	
	public Partition get(ColumnCollection key) {
		Partition result = this.get(key.cardinality()).get(key);
		if (USE_MEMORY_MANAGEMENT && result != null) {
			this.usageCounter.adjustValue(key, 1);
			this.leastRecentlyUsedPartitions.remove(key);
			this.leastRecentlyUsedPartitions.addLast(key);
			freeSpace();
		}
		return result; 
	}
	
	private void freeSpace() {
		if (this.getCount() > PARTITION_THRESHOLD + this.numberOfColumns) {
			System.out.println("Count before:\t" + this.getCount());
			int[] usageCounters = this.usageCounter.values();
			Arrays.sort(usageCounters);
			int medianOfUsage = 0;
			medianOfUsage = usageCounters[usageCounters.length / 2]; 
			if (usageCounters.length % 2 == 0) {
				medianOfUsage += usageCounters[usageCounters.length / 2 + 1];
				medianOfUsage /= 2;
			}
				
			int numberOfPartitionsToDelete = (PARTITION_THRESHOLD + this.numberOfColumns)/2;
			int deletedColumns = 0;
			Iterator<ColumnCollection> deleteIt = this.leastRecentlyUsedPartitions.iterator();
			while (deleteIt.hasNext() && deletedColumns < numberOfPartitionsToDelete) {
				ColumnCollection keyOfPartitionToDelete = deleteIt.next();
				if (!keyOfPartitionToDelete.isAtomic() && this.usageCounter.get(keyOfPartitionToDelete) <= medianOfUsage) {
					deleteIt.remove();
					this.removePartition(keyOfPartitionToDelete);
					this.usageCounter.remove(keyOfPartitionToDelete);
					deletedColumns++;
				}
			}
			System.out.println("Count after:\t" + this.getCount());
		}
	}
	
	public void addPartition(FileBasedPartition partition) {
		long cardinalityOfPartitionIndices = partition.getIndices().cardinality();
		this.get(cardinalityOfPartitionIndices).put(partition.getIndices(), partition);
		if (USE_MEMORY_MANAGEMENT) {
			this.leastRecentlyUsedPartitions.addLast(partition.getIndices());
			this.usageCounter.adjustOrPutValue(partition.getIndices(), 1, 1);
			this.totalCount.adjustOrPutValue(partition.getIndices(), 1, 1);
		}
	}
	
	public void addPartition(Partition partition) {
		long cardinalityOfPartitionIndices = partition.getIndices().cardinality();
		this.get(cardinalityOfPartitionIndices).put(partition.getIndices(), partition);
		if (USE_MEMORY_MANAGEMENT) {
			this.leastRecentlyUsedPartitions.addLast(partition.getIndices());
			this.usageCounter.adjustOrPutValue(partition.getIndices(), 1, 1);
			this.totalCount.adjustOrPutValue(partition.getIndices(), 1, 1);
		}
	}
	
	private void removePartition(ColumnCollection partitionKey) {
		long cardinalityOfPartitionIndices = partitionKey.cardinality();
		this.get(cardinalityOfPartitionIndices).remove(partitionKey);
	}
	
	public void addPartitions(ArrayList<Partition> partitions) {
		for (Partition partition : partitions) {
			this.addPartition(partition);
		}
	}
	
	public Partition getAtomicPartition(int columnIndex) {
		return this.get(0).get(this.key.clearAllCopy().setCopy(columnIndex));
	}
	
	public ArrayList<Partition> getBestMatchingPartitions(ColumnCollection path) {
		ColumnCollection pathCopy = (ColumnCollection) path.clone();
		ArrayList<Partition> bestMatchingPartitions = new ArrayList<>();
		long notCoveredColumns = pathCopy.cardinality();
		long sizeOfLastMatch = notCoveredColumns;
		
		// the strategy is greedy and fit first
		outer: while (notCoveredColumns > 0) {
			// we don't need to check the sizes above the last match size again
			for (long collectionCardinality = Math.min(notCoveredColumns, sizeOfLastMatch); collectionCardinality > 0; collectionCardinality--) {
				HashMap<ColumnCollection, Partition> candidatesOfLevel = this.get(collectionCardinality);
				for (ColumnCollection candidateOfLevel : candidatesOfLevel.keySet()) {
					if (candidateOfLevel.isSubsetOf(pathCopy)) {
//						bestMatchingPartitions.add(candidatesOfLevel.get(candidateOfLevel));
						bestMatchingPartitions.add(this.get(candidateOfLevel));
						notCoveredColumns -= collectionCardinality;
						pathCopy.remove(candidateOfLevel);
						sizeOfLastMatch = collectionCardinality;
						continue outer;
					}
				}
			}
		}
		return bestMatchingPartitions;
	}
}
