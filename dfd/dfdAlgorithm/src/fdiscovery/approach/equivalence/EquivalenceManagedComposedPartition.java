package fdiscovery.approach.equivalence;

import java.util.ArrayList;

import fdiscovery.equivalence.EquivalenceGroupTIntHashSet;
import fdiscovery.equivalence.TEquivalence;
import fdiscovery.partitions.Partition;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

public class EquivalenceManagedComposedPartition extends EquivalenceManagedPartition {

	private static final long serialVersionUID = 3559626986145010589L;

	public static EquivalenceManagedPartition buildPartition(ArrayList<EquivalenceManagedPartition> partitions) {
		
		// build base
		EquivalenceManagedPartition result = null;
		if (partitions.size() > 1) {
			result = partitions.get(0);
			
			for (int i=1; i<partitions.size(); i++) {
				result = new EquivalenceManagedComposedPartition(result, partitions.get(i));
			}
		} else if (partitions.size() == 1) {
			result = partitions.get(0);
		}
		
		return result;
	}
	
	public static ArrayList<EquivalenceManagedPartition> buildPartitions(ArrayList<EquivalenceManagedPartition> partitions) {
		ArrayList<EquivalenceManagedPartition> joinedPartitions = new ArrayList<>();
		
		// build base
		EquivalenceManagedPartition result = null;
		if (partitions.size() > 1) {
			result = partitions.get(0);
			
			for (int i=1; i<partitions.size(); i++) {
				result = new EquivalenceManagedComposedPartition(result, partitions.get(i));
				joinedPartitions.add(result);
			}
		} else if (partitions.size() == 1) {
			result = partitions.get(0);
		}
		
		return joinedPartitions;
	}
	
	public EquivalenceManagedComposedPartition(EquivalenceManagedPartition base, EquivalenceManagedPartition additional) {
		super(base, additional);

		if (base.size() > additional.size()) {
			EquivalenceManagedPartition swap = additional;
			additional = base;
			base = swap;
		}
		
		TIntObjectHashMap<TEquivalence> mapping = new TIntObjectHashMap<>();
		
		int[] probeTable = Partition.probeTable;
		int i = 1;
		for (TEquivalence equivalenceGroup : base) {
			for (TIntIterator valueIt = equivalenceGroup.iterator(); valueIt.hasNext(); ) {
				int value = valueIt.next();
				probeTable[value] = i;
			}
			mapping.put(i, new EquivalenceGroupTIntHashSet());
			i++;
		}
		
		for (TEquivalence equivalenceGroup : additional) {
			for (TIntIterator valueIt = equivalenceGroup.iterator(); valueIt.hasNext(); ) {
				int value = valueIt.next();
				if (probeTable[value] != -1) {
					TEquivalence old = mapping.get(probeTable[value]);
					old.add(value);
				}
			}
			for (TIntIterator valueIt = equivalenceGroup.iterator(); valueIt.hasNext(); ) {
				int value = valueIt.next();
				TEquivalence s = mapping.get(probeTable[value]);
				if (s != null && s.size() > 1) {
					this.add(s);
				}
				mapping.put(probeTable[value], new EquivalenceGroupTIntHashSet());
			}
		}
		i = 1;
		for (TEquivalence equivalenceGroup : base) {
			for (TIntIterator valueIt = equivalenceGroup.iterator(); valueIt.hasNext(); ) {
				int value = valueIt.next();
				this.hashNumber += value + 1;
				probeTable[value] = -1;
			}
		}
	}	
}
