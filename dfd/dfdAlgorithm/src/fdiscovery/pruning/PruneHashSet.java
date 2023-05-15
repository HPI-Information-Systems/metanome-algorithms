package fdiscovery.pruning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import fdiscovery.columns.ColumnCollection;

public class PruneHashSet extends HashMap<ColumnCollection, HashSet<ColumnCollection>> implements PruneInterface {

	private static final long serialVersionUID = 8012444410589325434L;

	public PruneHashSet(int numberOfColumns) {
		super(numberOfColumns);
		ColumnCollection key = new ColumnCollection(numberOfColumns);
		for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++) {
			this.put(key.setCopy(columnIndex), new HashSet<ColumnCollection>());
		}
	}
	
	public static ColumnCollection getNotPrunedKey(Dependencies dependencies, NonDependencies nonDependencies, ArrayList<ColumnCollection> candidates) {
		for (ColumnCollection candidate : candidates) {
			if (!dependencies.isRepresented(candidate) && !nonDependencies.isRepresented(candidate)) {
				return candidate;
			}
		}
		return null;
	}
	
	@Override
	public void rebalance() {
		boolean rebalancedGroup = false;
		
		do {
			rebalancedGroup = false;
			ArrayList<ColumnCollection> groupKeys = new ArrayList<>(this.keySet()); 
			for (ColumnCollection key : groupKeys) {
				if (this.get(key).size() > SPLIT_THRESHOLD) {
					rebalanceGroup(key);
					rebalancedGroup = true;
				}
			}
		} while (rebalancedGroup);
	}

	@Override
	public void rebalanceGroup(ColumnCollection groupKey) {
		HashSet<ColumnCollection> depsOfGroup = this.get(groupKey);
		for (int columnIndex : groupKey.complementCopy().getSetBits()) {
			ColumnCollection newKey = groupKey.setCopy(columnIndex);
			HashSet<ColumnCollection> newGroup = new HashSet<ColumnCollection>();
			this.put(newKey, newGroup);
			
			for (ColumnCollection depOfGroup : depsOfGroup) {
				// when splitting a group it cannot contain the key itself
				// because otherwise the group cannot contain any other 
				// element since it would be a superset of the key and be pruned
				// OR
				// when splitting a group it cannot contain the key itself
				// because otherwise all supersets of the key would have 
				// been pruned and it wouldn't need to be split
				if (newKey.isSubsetOf(depOfGroup)) {
					newGroup.add(depOfGroup);
				}
			}
		}
		// remove the old group
		this.remove(groupKey);		
	}
}
