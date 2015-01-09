package fdiscovery.equivalence;

import java.util.Set;
import java.util.TreeSet;


public class EquivalenceGroupTreeSet extends TreeSet<Integer> implements Comparable<EquivalenceGroupTreeSet>, Equivalence  {

	private static final long serialVersionUID = 8411462245069900864L;

	private int identifier;
	
	public EquivalenceGroupTreeSet() {
		this.identifier = Equivalence.unassignedIdentifier;
	}
	
	public EquivalenceGroupTreeSet(int identifier) {
		this.identifier = identifier;
	}
	
	@Override
	public int compareTo(EquivalenceGroupTreeSet o) {
		if (this.size() != o.size()) {
			return this.size() - o.size();
		} else {
			return this.first() - o.first();
		}
	}

	@Override
	public int getIdentifier() {
		return this.first();
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
		
		super.add(value);
	}
}
