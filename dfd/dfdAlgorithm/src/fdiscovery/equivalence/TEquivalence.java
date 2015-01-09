package fdiscovery.equivalence;

import gnu.trove.set.TIntSet;

public interface TEquivalence extends TIntSet {

	public int unassignedIdentifier = -1;
	
	public int getIdentifier();
	public <T extends TIntSet> boolean isProperSubset(T other);
	public boolean add(int value);
}
