package fdiscovery.approach.equivalence;

import java.util.ArrayList;
import java.util.HashMap;

import fdiscovery.columns.ColumnCollection;
import fdiscovery.partitions.Partition;
//import fdiscovery.pruning.PartitionEquivalences;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

public class EquivalenceManagedJoinedPartitions extends TLongObjectHashMap<HashMap<ColumnCollection, EquivalenceManagedPartition>> {

	private static final long serialVersionUID = -7385828030861564827L;
	
	private int numberOfColumns;
	private ColumnCollection key;
	private PartitionEquivalences equivalences;
	
	public EquivalenceManagedJoinedPartitions(int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
		this.equivalences = new PartitionEquivalences();
		this.key = new ColumnCollection(numberOfColumns);
		for (long cardinality = 1; cardinality <= this.numberOfColumns; cardinality++) {
			this.put(cardinality, new HashMap<ColumnCollection, EquivalenceManagedPartition>());
		}
	}

	public PartitionEquivalences getEquivalences() {
		return this.equivalences;
	}
	
	public THashMap<ColumnCollection, ArrayList<ColumnCollection>> getReversedEquivalenceMapping() {
		return this.equivalences.getReversedMapping();
	}
	
	public ArrayList<ColumnCollection> getEquivalences(ColumnCollection candidate, int currentRhs) {
		return this.equivalences.getEquivalentPartitionIndices(candidate, currentRhs);
	}
	 
	public void reset() {
		for (long cardinality = 2; cardinality <= this.numberOfColumns; cardinality++) {
			this.get(cardinality).clear();
		}
	}
	 
	public ColumnCollection getEquivalent(ColumnCollection referenceIndices) {
		return this.equivalences.getEquivalent(referenceIndices);
	}
	
	public int getCount() {
		int cumulatedCount = 0;
		for (HashMap<ColumnCollection, EquivalenceManagedPartition> elementsOfLevel : this.valueCollection()) {
			cumulatedCount += elementsOfLevel.size();
		}
		return cumulatedCount;
	}
	
	public EquivalenceManagedPartition get(ColumnCollection key) {
		EquivalenceManagedPartition result = this.get(key.cardinality()).get(key);
		return result; 
	}
	
	public void addPartition(EquivalenceManagedPartition partition) {
		long cardinalityOfPartitionIndices = partition.getIndices().cardinality();
		this.get(cardinalityOfPartitionIndices).put(partition.getIndices(), partition);
		this.equivalences.addPartition(partition);
	}
	
	public boolean equivalentPartitionExists(ColumnCollection partitionIndices) {
		return this.equivalences.equivalentPartitionExists(partitionIndices);
	}
	
	public EquivalenceManagedPartition getEquivalentPartition(ColumnCollection partitionKey) {
		return this.equivalences.getEquivalentPartition(this.get(partitionKey));
	}
	
	public void addPartitions(ArrayList<EquivalenceManagedPartition> partitions) {
		for (EquivalenceManagedPartition partition : partitions) {
			this.addPartition(partition);
		}
	}
	
	public Partition getAtomicPartition(int columnIndex) {
		return this.get(0).get(this.key.clearAllCopy().setCopy(columnIndex));
	}
	
	public ArrayList<EquivalenceManagedPartition> getBestMatchingPartitions(ColumnCollection path) {
		ColumnCollection pathCopy = (ColumnCollection) path.clone();
		ArrayList<EquivalenceManagedPartition> bestMatchingPartitions = new ArrayList<>();
		long notCoveredColumns = pathCopy.cardinality();
		long sizeOfLastMatch = notCoveredColumns;
		
		// the strategy is greedy and fit first
		outer: while (notCoveredColumns > 0) {
			// we don't need to check the sizes above the last match size again
			for (long collectionCardinality = Math.min(notCoveredColumns, sizeOfLastMatch); collectionCardinality > 0; collectionCardinality--) {
				HashMap<ColumnCollection, EquivalenceManagedPartition> candidatesOfLevel = this.get(collectionCardinality);
				for (ColumnCollection candidateOfLevel : candidatesOfLevel.keySet()) {
					if (candidateOfLevel.isSubsetOf(pathCopy)) {
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
