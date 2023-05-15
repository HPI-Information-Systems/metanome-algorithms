package fdiscovery.equivalence;

import java.util.HashSet;
import java.util.Set;

public class EquivalenceGroupHashSet extends HashSet<Integer> implements Comparable<EquivalenceGroupHashSet>, Equivalence  {

	private static final long serialVersionUID = 8411462245069900864L;

	private int identifier;
	
	public EquivalenceGroupHashSet() {
		this.identifier = Equivalence.unassignedIdentifier;
	}
	
	public EquivalenceGroupHashSet(int identifier) {
		this.identifier = identifier;
	}
	
	@Override
	public int compareTo(EquivalenceGroupHashSet o) {
		if (this.size() != o.size()) {
			return this.size() - o.size();
		}
		return this.identifier - o.identifier;
	}

	@Override
	public int getIdentifier() {
		return this.identifier;
	}

	@Override
	public <T extends Set<Integer>> boolean isProperSubset(T other) {
		if (this.size() >= other.size()) {
			return false;
		}
		
		return other.containsAll(this);
	}

	@Override
	public void add(int value) {
		if (this.identifier == Equivalence.unassignedIdentifier) {
			this.identifier = value;
		}
		
		super.add(Integer.valueOf(value));
	}
}