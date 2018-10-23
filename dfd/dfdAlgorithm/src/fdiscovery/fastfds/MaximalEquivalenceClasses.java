package fdiscovery.fastfds;

import fdiscovery.equivalence.TEquivalence;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.THashSet;

import java.util.HashSet;
import java.util.Iterator;

import fdiscovery.partitions.StrippedPartition;
import fdiscovery.partitions.StrippedPartitions;

public class MaximalEquivalenceClasses extends THashSet<TEquivalence> {

	private static final long serialVersionUID = 2744355690141458847L;

	public MaximalEquivalenceClasses(StrippedPartitions strippedPartitions) throws OutOfMemoryError {
		super();

		TIntObjectHashMap<HashSet<TEquivalence>> equivalenceGroupsBySize = new TIntObjectHashMap<>();
		int maximumGroupSize = 0;
		
		for (StrippedPartition strippedPartition : strippedPartitions.values()) {
			for (TEquivalence equivalenceGroup : strippedPartition) {
				int groupSize = equivalenceGroup.size();
				HashSet<TEquivalence> sizeGroup;
				if (!equivalenceGroupsBySize.contains(groupSize)) {
					sizeGroup = new HashSet<>();
					equivalenceGroupsBySize.put(groupSize, sizeGroup);
				} else {
					sizeGroup = equivalenceGroupsBySize.get(groupSize);
				}
				sizeGroup.add(equivalenceGroup);
			}
		}
		
		subset:
			for (int subsetGroupIndex = 1; subsetGroupIndex < maximumGroupSize; subsetGroupIndex++) {
				superset:
					for (int supersetGroupIndex = maximumGroupSize; supersetGroupIndex > subsetGroupIndex; supersetGroupIndex--) {
						HashSet<TEquivalence> subsetGroups = equivalenceGroupsBySize.get(subsetGroupIndex);
						HashSet<TEquivalence> supersetGroups = equivalenceGroupsBySize.get(supersetGroupIndex);
						if (subsetGroups == null || subsetGroups.isEmpty()) {
							continue subset;
						}
						if (supersetGroups == null || supersetGroups.isEmpty()) {
							continue superset;
						}

						for (Iterator<TEquivalence> subsetGroupsIt = subsetGroups.iterator(); subsetGroupsIt.hasNext();) {
							TEquivalence subsetGroup = subsetGroupsIt.next();
							for (TEquivalence supersetGroup : supersetGroups) {
								if (subsetGroup.isProperSubset(supersetGroup)) {
									subsetGroupsIt.remove();
									break;
								}
							}
						}
					}
			}
		
		for (int groupSize : equivalenceGroupsBySize.keys()) {
			for (TEquivalence sizeGroup : equivalenceGroupsBySize.get(groupSize)) {
				maximumGroupSize = Math.max(groupSize, maximumGroupSize);
				this.add(sizeGroup);
			}
		}
	}
		
	
	@Override
	public String toString() {
		StringBuilder outputBuilder = new StringBuilder();
		outputBuilder.append("MaximalEquivalenceClasses\n");
		outputBuilder.append("{");
		for (TEquivalence equivalenceGroup : this) {
			outputBuilder.append(equivalenceGroup.toString());
		}
		outputBuilder.append("}");
		outputBuilder.append("\n");
		outputBuilder.append("MaximalEquivalenceClasses\n");

		return outputBuilder.toString();
	}
}
