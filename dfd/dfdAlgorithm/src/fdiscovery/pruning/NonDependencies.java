package fdiscovery.pruning;

import java.util.HashSet;
import java.util.Iterator;

import fdiscovery.columns.ColumnCollection;
import gnu.trove.set.hash.THashSet;

public class NonDependencies extends PruneHashSet {

	private static final long serialVersionUID = 3160579586722511675L;

	public NonDependencies(int numberOfColumns) {
		super(numberOfColumns);
	}
	
	public THashSet<ColumnCollection> getPrunedSupersets(THashSet<ColumnCollection> supersets) {
		THashSet<ColumnCollection> prunedSupersets = new THashSet<>();
		for (ColumnCollection superset : supersets) {
			if (this.isRepresented(superset)) {
				prunedSupersets.add(superset);
			}
		}
		return prunedSupersets;
	}
	
	public boolean isRepresented(ColumnCollection value) {
		for (ColumnCollection keyForGroup : this.keySet()) {
			if (keyForGroup.isSubsetOf(value)) {
				for (ColumnCollection nonDependency : this.get(keyForGroup)) {
					// prune subsets of non-dependencies
					if (value.isSubsetOf(nonDependency)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean findSupersetOf(ColumnCollection value) {
		for (ColumnCollection keyForGroup : this.keySet()) {
			if (keyForGroup.isSubsetOf(value)) {
				for (ColumnCollection valueInGroup : this.get(keyForGroup)) {
					if (valueInGroup.isSupersetOf(value)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean findProperSupersetOf(ColumnCollection value) {
		for (ColumnCollection keyForGroup : this.keySet()) {
			if (keyForGroup.isSubsetOf(value)) {
				for (ColumnCollection valueInGroup : this.get(keyForGroup)) {
					if (valueInGroup.isProperSupersetOf(value)) {
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
				for (Iterator<ColumnCollection> nonDepIt = depsForKey.iterator(); nonDepIt.hasNext(); ) {
					ColumnCollection nonDep = nonDepIt.next();
					if (newEntry.isSubsetOf(nonDep)) {
						continue outer;
					}
					if (newEntry.isSupersetOf(nonDep)) {
						nonDepIt.remove();
					}
				}
				depsForKey.add(newEntry);
			}
		}
		this.rebalance();
	}

	public ColumnCollection getNotRepresentedSuperset(THashSet<ColumnCollection> uncheckedSupersets) {
		for (ColumnCollection uncheckedSuperset : uncheckedSupersets) {
			if (!this.findSupersetOf(uncheckedSuperset)) {
				return uncheckedSuperset;
			}
		}
		return null;
	}
	
	public ColumnCollection getNotRepresentedSuperset(THashSet<ColumnCollection> uncheckedSupersets, Dependencies dependencies) {
		for (ColumnCollection uncheckedSuperset : uncheckedSupersets) {
			if (!this.findSupersetOf(uncheckedSuperset) && !dependencies.isRepresented(uncheckedSuperset)) {
				return uncheckedSuperset;
			}
		}
		return null;
	}
	
}
