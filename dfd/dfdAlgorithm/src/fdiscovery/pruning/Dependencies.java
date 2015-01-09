package fdiscovery.pruning;

import java.util.HashSet;
import java.util.Iterator;

import fdiscovery.columns.ColumnCollection;
import gnu.trove.set.hash.THashSet;

public class Dependencies extends PruneHashSet {

	private static final long serialVersionUID = 6853361532152708964L;

	public Dependencies(int numberOfColumns) {
		super(numberOfColumns);
	}

	public THashSet<ColumnCollection> getPrunedSubsets(THashSet<ColumnCollection> subsets) {
		THashSet<ColumnCollection> prunedSubsets = new THashSet<>();
		for (ColumnCollection subset : subsets) {
			if (this.isRepresented(subset)) {
				prunedSubsets.add(subset);
			}
		}
		return prunedSubsets;
	}
	
	public boolean isRepresented(ColumnCollection value) {
		for (ColumnCollection keyForGroup : this.keySet()) {
			if (keyForGroup.isSubsetOf(value)) {
				for (ColumnCollection dependency : this.get(keyForGroup)) {
					// prune supersets of dependencies
					if (value.isSupersetOf(dependency)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean findSubsetOf(ColumnCollection value) {
		for (ColumnCollection keyForGroup : this.keySet()) {
			if (keyForGroup.isSubsetOf(value)) {
				for (ColumnCollection valueInGroup : this.get(keyForGroup)) {
					if (valueInGroup.isSubsetOf(value)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public boolean findProperSubsetOf(ColumnCollection value) {
		for (ColumnCollection keyForGroup : this.keySet()) {
			if (keyForGroup.isSubsetOf(value)) {
				for (ColumnCollection valueInGroup : this.get(keyForGroup)) {
					if (valueInGroup.isProperSubsetOf(value)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public void add(ColumnCollection newEntry) {
		outer: for (ColumnCollection key : this.keySet()) {
			if (key.isSubsetOf(newEntry)) {
				HashSet<ColumnCollection> depsForKey = this.get(key);
				for (Iterator<ColumnCollection> depIt = depsForKey.iterator(); depIt.hasNext(); ) {
					ColumnCollection dep = depIt.next();
					if (newEntry.isSupersetOf(dep)) {
						continue outer;
					}
					if (newEntry.isSubsetOf(dep)) {
						depIt.remove();
					}
				}
				depsForKey.add(newEntry);
			}
		}
		this.rebalance();
	}

	public ColumnCollection getNotRepresentedSubset(THashSet<ColumnCollection> uncheckedSubsets) {
		for (ColumnCollection uncheckedSubset : uncheckedSubsets) {
			if (!this.findSubsetOf(uncheckedSubset)) {
				return uncheckedSubset;
			}
		}
		return null;
	}

}
