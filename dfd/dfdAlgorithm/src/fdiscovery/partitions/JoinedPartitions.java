package fdiscovery.partitions;

import java.util.ArrayList;
import java.util.HashMap;

import fdiscovery.columns.ColumnCollection;

public class JoinedPartitions extends HashMap<ColumnCollection, Partition> {

	private static final long serialVersionUID = -7385828030861564827L;

	private int numberOfColumns;
	private ColumnCollection key;
	
	public JoinedPartitions(int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
		this.key = new ColumnCollection(numberOfColumns);
	}
	
	public void addPartition(Partition partition) {
		this.put(partition.getIndices(), partition);
	}
	
	public void addPartitions(ArrayList<Partition> partitions) {
		for (Partition partition : partitions) {
			this.put(partition.getIndices(),partition);
		}
	}
	
	public Partition getAtomicPartition(int columnIndex) {
		this.key.clear(0, this.numberOfColumns);
		
		this.key.set(columnIndex);
		return this.get(this.key);
	}
	
	public ArrayList<Partition> getBestMatchingPartitionsLazy(ColumnCollection path) {
		ArrayList<Partition> bestMatchingPartitions = new ArrayList<>();

		for (int columnIndex : path.getSetBits()) {
			bestMatchingPartitions.add(this.getAtomicPartition(columnIndex));
		}
		
		return bestMatchingPartitions;
	}
	
	public ArrayList<Partition> getBestMatchingPartitions(ColumnCollection path) {
		ColumnCollection pathCopy = (ColumnCollection) path.clone();
		ArrayList<Partition> bestMatchingPartitions = new ArrayList<>();
		long notCoveredColumns = pathCopy.cardinality();

		// the strategy is greedy and fit first
		while (notCoveredColumns > 0) {
			long maxCoverCount = 0;
			Partition maxCoverPartition = null;
			for (ColumnCollection savedCollection : this.keySet()) {
				if (savedCollection.isSubsetOf(pathCopy)) {
					long currentCoverCount = ColumnCollection.intersectionCount(pathCopy, savedCollection);
					if (currentCoverCount > maxCoverCount) {
						maxCoverCount = currentCoverCount; 
						maxCoverPartition = this.get(savedCollection);
					}
				}
			}
			pathCopy.remove(maxCoverPartition.indices);
			notCoveredColumns = pathCopy.cardinality();
			bestMatchingPartitions.add(maxCoverPartition);
		}
		
		return bestMatchingPartitions;
	}
}
