package fdiscovery.approach.equivalence;

import java.util.ArrayList;

import fdiscovery.columns.ColumnCollection;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.THashSet;

public class PartitionEquivalences extends THashMap<ColumnCollection, EquivalenceManagedPartition> {
	
	private TLongObjectHashMap<TIntObjectHashMap<THashSet<EquivalenceManagedPartition>>> partitionHashes;
	private THashMap<ColumnCollection, ArrayList<ColumnCollection>> reversedMapping;
	private THashSet<ColumnCollection> observedPartitions;
	
	public PartitionEquivalences() {
		partitionHashes = new TLongObjectHashMap<>();
		observedPartitions = new THashSet<>();
		reversedMapping = new THashMap<>();
	}
	
	public THashMap<ColumnCollection, ArrayList<ColumnCollection>> getReversedMapping() {
		return this.reversedMapping;
	}
	
	private boolean containsSimilarPartition(EquivalenceManagedPartition referencePartition) {
		ColumnCollection referencePartitionKey = referencePartition.getIndices();
		for (ColumnCollection partitionKey : this.keySet()) {
			if (partitionKey.isSubsetOf(referencePartitionKey)) {
				return true;
			}
		}
		return false;
	}
	
	public void addPartition(EquivalenceManagedPartition partition) {
		if (!this.observedPartitions.contains(partition.getIndices()) && !this.containsSimilarPartition(partition)) {
			this.observedPartitions.add(partition.getIndices());
			long hashNumber = partition.getHashNumber();
			System.out.println(String.format("Partition[%s]\t%d\tSize: %d", partition.getIndices(), hashNumber, partition.size()));
			partitionHashes.putIfAbsent(hashNumber, new TIntObjectHashMap<THashSet<EquivalenceManagedPartition>>());
			partitionHashes.get(hashNumber).putIfAbsent(partition.size(), new THashSet<EquivalenceManagedPartition>());
			THashSet<EquivalenceManagedPartition> partitionGroup = partitionHashes.get(hashNumber).get(partition.size());

			if (partitionGroup.isEmpty()) {
				partitionGroup.add(partition);
			} else {
				// then there is at least one element in the partitionGroup
				checkPossibleEquivalences(partitionGroup, partition);
			}
		}
	}
	
	public ArrayList<ColumnCollection> getEquivalentPartitionIndices(ColumnCollection referenceIndices, int currentRhs) {
		ArrayList<ColumnCollection> equivalentPartitionIndices = new ArrayList<>();
		for (ColumnCollection reversedMappingKey : reversedMapping.keySet()) {
			if (reversedMappingKey.isSubsetOf(referenceIndices)) {
				for (ColumnCollection equivalenceIndices : this.reversedMapping.get(reversedMappingKey)) {
					ColumnCollection equivalentIndices = equivalenceIndices.orCopy(referenceIndices).removeCopy(reversedMappingKey);
					if (!equivalenceIndices.get(currentRhs)) {
						equivalentPartitionIndices.add(equivalentIndices);
					}
				}
			}
		}
		return equivalentPartitionIndices;
	}
	
	public ColumnCollection getEquivalent(ColumnCollection referenceIndices) {
		if (this.contains(referenceIndices)) {
			return this.get(referenceIndices).getIndices();
		}
		return null;
	}
	
	public boolean equivalentPartitionExists(ColumnCollection referenceIndices) {
		for (ColumnCollection equivalenceIndices : this.keySet()) {
			if (equivalenceIndices.isSubsetOf(referenceIndices)) {
				return true;
			}
		}
		return false;
	}
	
	public EquivalenceManagedPartition getEquivalentPartition(EquivalenceManagedPartition reference) {
		return this.get(reference);
	}
	
	private void checkPossibleEquivalences(THashSet<EquivalenceManagedPartition> referencePartitions, EquivalenceManagedPartition newPartition) {
		for (EquivalenceManagedPartition referencePartition : referencePartitions) {
			if (!referencePartition.getIndices().isSubsetOrSupersetOf(newPartition.getIndices())) {
				if (referencePartition.equals(newPartition)) {
					this.put(newPartition.getIndices(), referencePartition);
					this.reversedMapping.putIfAbsent(referencePartition.getIndices(), new ArrayList<ColumnCollection>());
					this.reversedMapping.get(referencePartition.getIndices()).add(newPartition.getIndices());
					System.out.println(String.format("Equivalence:[%s]~[%s]", newPartition.getIndices(), referencePartition.getIndices()));
					break;
				}
			}
		}
	}
}
