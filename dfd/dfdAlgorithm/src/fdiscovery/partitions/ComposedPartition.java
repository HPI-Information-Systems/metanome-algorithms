package fdiscovery.partitions;

import java.util.ArrayList;

import fdiscovery.equivalence.EquivalenceGroupTIntHashSet;
import fdiscovery.equivalence.TEquivalence;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

public class ComposedPartition extends Partition {

	private static final long serialVersionUID = 3559626986145010589L;

	public static Partition buildPartition(ArrayList<Partition> partitions) {
		
		// build base
		Partition result = null;
		if (partitions.size() > 1) {
			result = partitions.get(0);
			
			for (int i=1; i<partitions.size(); i++) {
				result = new ComposedPartition(result, partitions.get(i));
			}
		} else if (partitions.size() == 1) {
			result = partitions.get(0);
		}
		
		return result;
	}
	
	public static ArrayList<Partition> buildPartitions(ArrayList<Partition> partitions) {
		ArrayList<Partition> joinedPartitions = new ArrayList<>();
		
		// build base
		Partition result = null;
		if (partitions.size() > 1) {
			result = partitions.get(0);
			
			for (int i=1; i<partitions.size(); i++) {
				result = new ComposedPartition(result, partitions.get(i));
				joinedPartitions.add(result);
			}
		} else if (partitions.size() == 1) {
			result = partitions.get(0);
		}
		
		return joinedPartitions;
	}
	
	public ComposedPartition(Partition base, Partition additional) {
		super(base, additional);

		if (base.size() > additional.size()) {
			Partition swap = additional;
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
				probeTable[value] = -1;
			}
		}
	}	
}
