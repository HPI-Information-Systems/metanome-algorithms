package fdiscovery.columns;

import java.util.PriorityQueue;

public class Seeds extends PriorityQueue<Seed> {

	private static final long serialVersionUID = 3497425762452970552L;

	public boolean containsSubset(Seed seed) {
		for (Seed seedInQueue : this) {
			if (seedInQueue.getIndices().isProperSubsetOf(seed.getIndices())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsSuperset(Seed seed) {
		for (Seed seedInQueue : this) {
			if (seedInQueue.getIndices().isProperSupersetOf(seed.getIndices())) {
				return true;
			}
		}
		return false;
	}
}
