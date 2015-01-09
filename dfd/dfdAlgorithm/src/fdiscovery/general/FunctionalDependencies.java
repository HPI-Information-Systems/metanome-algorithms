package fdiscovery.general;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import fdiscovery.columns.ColumnCollection;
import gnu.trove.map.hash.THashMap;

public class FunctionalDependencies extends THashMap<ColumnCollection, ColumnCollection> {

	private static final long serialVersionUID = -6781621109409590322L;

	public ArrayList<ColumnCollection> getLHSForRHS(int rhsIndex) {
		ArrayList<ColumnCollection> lhsForRHS = new ArrayList<>();
		for (ColumnCollection lhs : this.keySet()) {
			if (this.get(lhs).get(rhsIndex)) {
				lhsForRHS.add(lhs);
			}
		}
		return lhsForRHS;
	}
	
	public void minimize(int rhsIndex) {
		ArrayList<ColumnCollection> lhsForRhsToDelete = this.getLHSForRHS(rhsIndex);
		for (Iterator<ColumnCollection> valueIt = lhsForRhsToDelete.iterator(); valueIt.hasNext(); ) {
			ColumnCollection value = valueIt.next();
			boolean supersetFound = false;
			for (ColumnCollection candidate : this.getLHSForRHS(rhsIndex)) {
				if (candidate.isProperSupersetOf(value)) {
					supersetFound = true;
					break;
				}
			}
			if (!supersetFound) {
				valueIt.remove();
			}
		}
		for (ColumnCollection lhs : this.keySet()) {
			if (lhsForRhsToDelete.contains(lhs)) {
				ColumnCollection rhs = this.get(lhs);
				this.put(lhs, rhs.removeColumnCopy(rhsIndex));
				System.out.println(String.format("Remove %s->%s", lhs, (char)(rhsIndex + 65)));
			}
		}
	}
	
	public void addEquivalenceDependency(ColumnCollection reference, ColumnCollection equivalence) {
		Set<ColumnCollection> lhsKeys = new HashSet<ColumnCollection>(this.keySet());
		for (ColumnCollection lhsKey : lhsKeys) {
			if (reference.isSubsetOf(lhsKey) && !equivalence.isSubsetOf(lhsKey)) {
				ColumnCollection equivalenceKey = lhsKey.removeCopy(reference);
				equivalenceKey.or(equivalence);
				ColumnCollection rhsForLhs = this.get(lhsKey).removeCopy(equivalence);
				if (!this.containsKey(equivalenceKey)) {
					this.put(equivalenceKey, rhsForLhs);
				} else {
					this.get(equivalenceKey).or(rhsForLhs);
				}
			} 
		}
		this.addRHSColumn(equivalence, reference.nextSetBit(0));
	}
	
	public void addEquivalenceNonDependency(ColumnCollection reference, ColumnCollection equivalence) {
		Set<ColumnCollection> lhsKeys = new HashSet<ColumnCollection>(this.keySet());
		for (ColumnCollection lhsKey : lhsKeys) {
			if (reference.isSubsetOf(lhsKey) && !equivalence.isSubsetOf(lhsKey)) {
				ColumnCollection equivalenceKey = lhsKey.removeCopy(reference);
				equivalenceKey.or(equivalence);
				this.put(equivalenceKey, this.get(lhsKey));
			} 
		}
	}
	
	public int getNumberOfNonAtomicDependencies() {
		int nonAtomicFDCount = 0;
		for (ColumnCollection determining : this.keySet()) {
			if (!determining.isAtomic()) {
				nonAtomicFDCount += this.get(determining).cardinality();
			}
		}
		return nonAtomicFDCount;
	}
	
	public void addRHSColumn(ColumnCollection lhs, Integer rhsIndex) {
		ColumnCollection rhs = null;
		if (!this.containsKey(lhs)) {
			rhs = new ColumnCollection(lhs.getNumberOfColumns());
			this.put(lhs, rhs);
		} else {
			rhs = this.get(lhs);
		}
		rhs.set(rhsIndex);
	}
	
	public int getCount() {
		int fdCount = 0;
		
		for (ColumnCollection determining : this.keySet()) {
			fdCount += this.get(determining).cardinality();
		}
		
		return fdCount;
	}
	
	public int getCount(int lhsSize) {
		int fdCount = 0;
		for (ColumnCollection determining : this.keySet()) {
			if (determining.cardinality() == lhsSize) {
				fdCount += this.get(determining).cardinality();
			}
		}
		return fdCount;
	}
	
	public int getCountForSizeGreaterThan(int lhsSize) {
		int fdCount = 0;
		for (ColumnCollection determining : this.keySet()) {
			if (determining.cardinality() > lhsSize) {
				fdCount += this.get(determining).cardinality();
			}
		}
		return fdCount;
	}
	
	public int getCountForSizeLesserThan(int lhsSize) {
		int fdCount = 0;
		for (ColumnCollection determining : this.keySet()) {
			if (determining.cardinality() < lhsSize) {
				fdCount += this.get(determining).cardinality();
			}
		}
		return fdCount;
	}
	
	public String toString() {
		StringBuilder outputBuilder = new StringBuilder();

		for (ColumnCollection determining : this.keySet()) {
			for (Integer dependentColumn : this.get(determining).getSetBits()) {
				for (Integer determiningColumn : determining.getSetBits()) {
					outputBuilder.append(String.format("c%04d\t", determiningColumn));
				}
				outputBuilder.append(String.format("->\tc%04d\n", dependentColumn));
			}
		}
		return outputBuilder.toString();
	}
}
