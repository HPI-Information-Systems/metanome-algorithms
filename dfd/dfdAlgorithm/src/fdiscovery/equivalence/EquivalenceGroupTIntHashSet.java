package fdiscovery.equivalence;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class EquivalenceGroupTIntHashSet extends TIntHashSet implements Comparable<EquivalenceGroupTIntHashSet>, TEquivalence {

	private int identifier;
	
	public EquivalenceGroupTIntHashSet() {
		this.identifier = Equivalence.unassignedIdentifier;
	}
	
	public EquivalenceGroupTIntHashSet(int identifier) {
		this.identifier = identifier;
	}
	
	@Override
	public int getIdentifier() {
		return this.identifier;
	}
	
	@Override
	public <T extends TIntSet> boolean isProperSubset(T other) {
		if (this.size() >= other.size()) {
			return false;
		}
		
		return other.containsAll(this);
	}

	@Override
	public boolean add(int value) {
		if (this.identifier == Equivalence.unassignedIdentifier) {
			this.identifier = value;
		}
		
		return super.add(value);
	}

	@Override
	public int compareTo(EquivalenceGroupTIntHashSet o) {
		if (this.size() != o.size()) {
			return this.size() - o.size();
		} else {
			return this.identifier - o.identifier;
		}		
	}
}
