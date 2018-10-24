package de.metanome.algorithms.normalize.fdextension;

import java.util.BitSet;
import java.util.Map;
import java.util.Map.Entry;

import de.metanome.algorithms.normalize.aspects.NormiPersistence;
import de.metanome.algorithms.normalize.utils.Utils;

public class NaiveFdExtender extends FdExtender {

	public NaiveFdExtender(NormiPersistence persister, String tempResultsPath) {
		super(persister, tempResultsPath);
	}

	@Override
	public void executeAlgorithm(Map<BitSet, BitSet> fds) {
		System.out.println("Building the FDs' closures ...");
		for (Entry<BitSet, BitSet> entry : fds.entrySet()) {
			BitSet lhs = entry.getKey();
			BitSet rhs = entry.getValue();
			
			// Add the trivial fds to the current fd's rhs for the closure construction (we need to check against ALL the attributes in this fd to build its closure)
			rhs.or(lhs);
			
			// Extend rhs
			long rhsCardinality;
			do {
				rhsCardinality = rhs.cardinality();
				
				for (Entry<BitSet, BitSet> other : fds.entrySet())
					if (Utils.andNotCount(other.getKey(), rhs) == 0)
						rhs.or(other.getValue());
				
				// TODO: when we think of parallelizing the closure calculation
			//	closureFds.entrySet().stream()
			//		.filter(other -> {return Utils.andNotCount(other.getKey(), rhs) == 0;})
			//		.forEach(other -> rhs.and(other.getValue()));
			}
			while (rhsCardinality != rhs.cardinality());
			
			// Remove the trivial fds
			rhs.andNot(lhs);
		}
	}

}
