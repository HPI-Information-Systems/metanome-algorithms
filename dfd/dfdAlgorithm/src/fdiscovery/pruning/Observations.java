package fdiscovery.pruning;

import java.util.HashMap;

import fdiscovery.approach.ColumnOrder;
import fdiscovery.columns.ColumnCollection;
import gnu.trove.set.hash.THashSet;

public class Observations extends HashMap<ColumnCollection, Observation> {

	private static final long serialVersionUID = 2932117192054503664L;
	
	public ColumnCollection getUncheckedMaximalSubset(ColumnCollection lhs) {
		for (int columnIndex : lhs.getSetBits()) {
			ColumnCollection subsetIndices = lhs.removeColumnCopy(columnIndex);
			if (!this.containsKey(subsetIndices)) {
				return subsetIndices;
			}
		}
		return null;
	}
	
	public THashSet<ColumnCollection> getUncheckedMaximalSubsets(ColumnCollection lhs, ColumnOrder order) {
		THashSet<ColumnCollection> uncheckedMaximalSubsets = new THashSet<>();
		
//		if (lhs.cardinality() > 2) {
			for (int columnIndex : order.getOrderHighDistinctCount(lhs)) { 
				ColumnCollection subsetIndices = lhs.removeColumnCopy(columnIndex);
				if (!this.containsKey(subsetIndices)) {
					uncheckedMaximalSubsets.add(subsetIndices);
				}
			}
//		}
		return uncheckedMaximalSubsets;
	}
	
	public THashSet<ColumnCollection> getUncheckedOrCandidateMaximalSubsets(ColumnCollection lhs, ColumnOrder order) {
		THashSet<ColumnCollection> uncheckedMaximalSubsets = new THashSet<>();
		
		// we only want to check subsets with at least 2 columns
		if (lhs.cardinality() > 2) {
			for (int columnIndex : order.getOrderHighDistinctCount(lhs)) { 
				ColumnCollection subsetIndices = lhs.removeColumnCopy(columnIndex);
				if (!this.containsKey(subsetIndices) || this.get(subsetIndices) == Observation.CANDIDATE_MINIMAL_DEPENDENCY) {
					uncheckedMaximalSubsets.add(subsetIndices);
				}
			}
		}
		return uncheckedMaximalSubsets;
	}
	
	public THashSet<ColumnCollection> getMaximalSubsets(ColumnCollection lhs, ColumnOrder order) {
		THashSet<ColumnCollection> uncheckedMaximalSubsets = new THashSet<>();
		
		// we only want to check subsets with at least 2 columns
		if (lhs.cardinality() > 2) {
			for (int columnIndex : order.getOrderHighDistinctCount(lhs)) { 
				ColumnCollection subsetIndices = lhs.removeColumnCopy(columnIndex);
				uncheckedMaximalSubsets.add(subsetIndices);
			}
		}
		return uncheckedMaximalSubsets;
	}
	
	public ColumnCollection getUncheckedMinimalSuperset(ColumnCollection lhs, int rhsIndex) {
		for (int columnIndex : lhs.setCopy(rhsIndex).complement().getSetBits()) {
			ColumnCollection supersetIndices = lhs.setCopy(columnIndex);
			if (!this.containsKey(supersetIndices)) {
				return supersetIndices;
			}
		}
		return null;
	} 
	
	public THashSet<ColumnCollection> getUncheckedOrCandidateMinimalSupersets(ColumnCollection lhs, int rhsIndex, ColumnOrder order) {
		THashSet<ColumnCollection> uncheckedMinimalSupersets = new THashSet<>();
		
		for (int columnIndex : order.getOrderLowDistinctCount(lhs.setCopy(rhsIndex).complement())) {
			ColumnCollection supersetIndices = lhs.setCopy(columnIndex);
			if (!this.containsKey(supersetIndices) || this.get(supersetIndices) == Observation.CANDIDATE_MAXIMAL_NON_DEPENDENCY) {
				uncheckedMinimalSupersets.add(supersetIndices);
			}
		}
		return uncheckedMinimalSupersets;
	}
	
	public THashSet<ColumnCollection> getUncheckedMinimalSupersets(ColumnCollection lhs, int rhsIndex, ColumnOrder order) {
		THashSet<ColumnCollection> uncheckedMinimalSupersets = new THashSet<>();
		
		for (int columnIndex : order.getOrderLowDistinctCount(lhs.setCopy(rhsIndex).complement())) {
			ColumnCollection supersetIndices = lhs.setCopy(columnIndex);
			if (!this.containsKey(supersetIndices)) {
				uncheckedMinimalSupersets.add(supersetIndices);
			}
		}
		return uncheckedMinimalSupersets;
	} 
	
	public THashSet<ColumnCollection> getMinimalSupersets(ColumnCollection lhs, int rhsIndex, ColumnOrder order) {
		THashSet<ColumnCollection> uncheckedMinimalSupersets = new THashSet<>();

		for (int columnIndex : order.getOrderLowDistinctCount(lhs.setCopy(rhsIndex).complement())) {
			ColumnCollection supersetIndices = lhs.setCopy(columnIndex);
			uncheckedMinimalSupersets.add(supersetIndices);
		}
		return uncheckedMinimalSupersets;
	} 
	
	public Observation updateDependencyType(ColumnCollection lhs) {
		if (lhs.cardinality() > 1) {
			boolean foundUncheckedSubset = false;
			for (int columnIndex : lhs.getSetBits()) {
				Observation observationOfSubset = this.get(lhs.removeColumnCopy(columnIndex));
				if (observationOfSubset == null) {
					foundUncheckedSubset = true;
				} else if (observationOfSubset.isDependency()) {
					return Observation.DEPENDENCY;
				} 
			}
			if (foundUncheckedSubset) {
				return Observation.CANDIDATE_MINIMAL_DEPENDENCY;
			}
		}
		return Observation.MINIMAL_DEPENDENCY;
	}
	
	public Observation updateNonDependencyType(ColumnCollection lhs, int rhsIndex) {
		boolean foundUncheckedSuperset = false;
		for (int columnIndex : lhs.setCopy(rhsIndex).complementCopy().getSetBits()) {
			Observation observationOfSuperset = this.get(lhs.setCopy(columnIndex));
			if (observationOfSuperset == null) {
				foundUncheckedSuperset = true;
			} else if (observationOfSuperset.isNonDependency()) {
				return Observation.NON_DEPENDENCY;
			}
		}
		if (foundUncheckedSuperset) {
			return Observation.CANDIDATE_MAXIMAL_NON_DEPENDENCY;
		}
		
		return Observation.MAXIMAL_NON_DEPENDENCY;
	}
}
