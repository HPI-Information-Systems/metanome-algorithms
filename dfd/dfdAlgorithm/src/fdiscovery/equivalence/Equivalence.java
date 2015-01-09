package fdiscovery.equivalence;

import java.util.Set;

public interface Equivalence extends Set<Integer> {

	public int unassignedIdentifier = -1;
	
	public int getIdentifier();
	public <T extends Set<Integer>> boolean isProperSubset(T other);
	public void add(int value);
}
