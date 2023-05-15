package fdiscovery.fastfds;

import java.awt.Point;

import fdiscovery.equivalence.TEquivalence;
import fdiscovery.partitions.StrippedPartition;
import fdiscovery.partitions.StrippedPartitions;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

public class EquivalenceClasses extends TIntObjectHashMap<EquivalenceClass> {

	private static final long serialVersionUID = 2744355690141458847L;

	public EquivalenceClasses(StrippedPartitions strippedPartitions) {
		int columnIndex = 0;
		for (StrippedPartition strippedPartition : strippedPartitions.values()) {
			for(TEquivalence equivalenceGroup : strippedPartition) {
				for (TIntIterator currentTupleIdIt=equivalenceGroup.iterator(); currentTupleIdIt.hasNext(); ) {
					int currentTupleId = currentTupleIdIt.next();
					this.putIfAbsent(currentTupleId, new EquivalenceClass());
					this.get(currentTupleId).add(new Point(columnIndex, equivalenceGroup.getIdentifier()));
				}
			}
			columnIndex++;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder outputBuilder = new StringBuilder();
		outputBuilder.append("EquivalenceClasses\n");

		for (TIntObjectIterator<EquivalenceClass> it = this.iterator(); it.hasNext(); ) {
			it.advance();
			outputBuilder.append(String.format("ec(%d(\t", it.key()));
			outputBuilder.append(String.format("{%s}\n", it.value().toString()));
		}
		outputBuilder.append("EquivalenceClasses\n");

		return outputBuilder.toString();
	}
}
