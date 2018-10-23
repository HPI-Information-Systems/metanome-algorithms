package fdiscovery.partitions;

import fdiscovery.equivalence.TEquivalence;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;

public class ProbeTable extends TIntIntHashMap {

	public ProbeTable(Partition partition) {
		// set the no entry key and no entry value to something recognizable
		super(partition.size(), 2, -1, -1);
		// The probe table maps each tuple index to the index of the equivalence class it belongs to
		for (TEquivalence equivalenceGroup : partition) {
			int groupIdentifier = equivalenceGroup.getIdentifier();
			for (TIntIterator groupValueIt=equivalenceGroup.iterator(); groupValueIt.hasNext(); ) {
//			for (Integer groupValue : equivalenceGroup) {
				this.put(groupValueIt.next(), groupIdentifier);
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder outputBuilder = new StringBuilder();
		outputBuilder.append("ProbeTable:\n");
		for (int key : this.keys()) {
			outputBuilder.append(String.format("%d\t->\t%d\n", Integer.valueOf(key), Integer.valueOf(this.get(key))));
		}
		
		return outputBuilder.toString();
	}
	
}
