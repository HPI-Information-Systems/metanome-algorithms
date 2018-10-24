package de.metanome.algorithms.normalize.fdextension;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.metanome.algorithms.normalize.aspects.NormiPersistence;

/** For each FD, test which other FDs it can extend **/
public class PushingFdExtender extends FdExtender {

	public PushingFdExtender(NormiPersistence persister, String tempResultsPath) {
		super(persister, tempResultsPath);
	}

	@Override
	public void executeAlgorithm(Map<BitSet, BitSet> fds) {
		System.out.println("Building the FDs' closures ...");
		
		// Sort FDs by lhs size
		List<Map.Entry<BitSet, BitSet>> sortedFds = new ArrayList<>(fds.entrySet());
		Collections.sort(sortedFds, new Comparator<Map.Entry<BitSet, BitSet>>() {
			@Override
			public int compare(Entry<BitSet, BitSet> o1, Entry<BitSet, BitSet> o2) {
				return o1.getKey().cardinality() - o2.getKey().cardinality();
			}
		});
		
		// For each FD, test if it extends some other FDs
		boolean somethingChanged = false;
		do {
			for (Entry<BitSet, BitSet> pivotFd : sortedFds) {
				BitSet pivotLhs = pivotFd.getKey();
				BitSet pivotRhs = pivotFd.getValue();
				
				for (Entry<BitSet, BitSet> otherFd : sortedFds) {
					if (pivotFd == otherFd)
						continue;
					
					BitSet otherLhs = pivotFd.getKey();
					BitSet otherRhs = pivotFd.getValue();
					
					BitSet pivotLhsCopy = (BitSet) pivotLhs.clone();
					pivotLhsCopy.andNot(otherLhs);
					pivotLhsCopy.andNot(otherRhs);
					
					if (pivotLhsCopy.cardinality() == 0) {
						otherRhs.or(pivotRhs);
						somethingChanged = true;
					}
				}
			}
		}
		while (somethingChanged);
	}

}
