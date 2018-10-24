package de.metanome.algorithms.normalize.fdextension;

import java.util.BitSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.metanome.algorithms.normalize.aspects.NormiPersistence;
import de.metanome.algorithms.normalize.structures.LhsTree;

/** For each FD, test the missing rhss if they can be added with some other FD (search based on prefix trees) **/
public class PullingFdExtender extends FdExtender {

	int numAttributes;
	boolean fdResultSetsComplete;
	
	public PullingFdExtender(NormiPersistence persister, String tempResultsPath, int numAttributes, boolean fdResultSetsComplete) {
		super(persister, tempResultsPath);
		this.numAttributes = numAttributes;
		this.fdResultSetsComplete = fdResultSetsComplete;
	}

	@Override
	public void executeAlgorithm(Map<BitSet, BitSet> fds) {
		System.out.println("Building the FDs' closures ...");
		
		// 1. Build the lhs trees
		System.out.print("\tBuilding tree ...");
		long time = System.currentTimeMillis();
		
		LhsTree[] lhsTrees = new LhsTree[this.numAttributes];
		for (int i = 0; i < this.numAttributes; i++)
			lhsTrees[i] = new LhsTree(this.numAttributes);
		
		for (Map.Entry<BitSet, BitSet> entry : fds.entrySet())
			for (int rhsAttribute = entry.getValue().nextSetBit(0); rhsAttribute >= 0; rhsAttribute = entry.getValue().nextSetBit(rhsAttribute + 1))
				lhsTrees[rhsAttribute].add(entry.getKey());
		System.out.println((System.currentTimeMillis() - time) + " ms");
		
		// 2. Expand the minimalFds
		System.out.print("\tExpanding FDs ...");
		time = System.currentTimeMillis();
		
/*		int lastAddedRhsAttribute;
		int currentRhsAttribute;
		for (Entry<BitSet, BitSet> entry : minimalFds.entrySet()) {
			BitSet lhs = entry.getKey();
			BitSet rhs = entry.getValue();
			
			// Add the trivial fds to the current fd's rhs for the closure construction (we need to check against ALL the attributes in this fd to build its closure)
			rhs.or(lhs);
			
			// Extend rhs
			lastAddedRhsAttribute = -1;
			currentRhsAttribute = 0;
			do {
				if (!rhs.get(currentRhsAttribute)) {
					if (lhsTrees[currentRhsAttribute].containsLhsOrSubset(rhs)) {
						rhs.set(currentRhsAttribute);
						lastAddedRhsAttribute = currentRhsAttribute;
					}
				}
				currentRhsAttribute = (currentRhsAttribute + 1) % numAttributes;
			}
			while ((currentRhsAttribute != lastAddedRhsAttribute) && !((currentRhsAttribute == 0) && (lastAddedRhsAttribute < 0)));
			
			// Remove the trivial fds
			rhs.andNot(lhs);
		}
*/		
/*		// Parallel streams are currently broken: 1. they all use the same global fork-join-executor, and 2. they pre-partition the work so that worker with larger tasks run longer
		minimalFds.entrySet().parallelStream().forEach(entry -> {
			BitSet lhs = entry.getKey();
			BitSet rhs = entry.getValue();
			
			// Add the trivial fds to the current fd's rhs for the closure construction (we need to check against ALL the attributes in this fd to build its closure)
			rhs.or(lhs);
			
			// Extend rhs
			int lastAddedRhsAttribute = -1;
			int currentRhsAttribute = 0;
			while ((currentRhsAttribute != lastAddedRhsAttribute) && !((currentRhsAttribute == numAttributes) && (lastAddedRhsAttribute < 0))) {
				currentRhsAttribute = currentRhsAttribute % numAttributes;
				if (!rhs.get(currentRhsAttribute)) {
					if (lhsTrees[currentRhsAttribute].containsLhsOrSubset(rhs)) {
						rhs.set(currentRhsAttribute);
						lastAddedRhsAttribute = currentRhsAttribute;
					}
				}
				currentRhsAttribute++;
			}
			
			// Remove the trivial fds
			rhs.andNot(lhs);
		});
*/		
/*	*/	
		// Initialize thread pool
		int numThreads = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		
		// Submit all FDs as extension tasks
		if (this.fdResultSetsComplete)
			for (Entry<BitSet, BitSet> entry : fds.entrySet())
				executor.submit(new ExtensionTaskCompleteMinimal(entry.getKey(), entry.getValue(), lhsTrees, this.numAttributes));
		else
			for (Entry<BitSet, BitSet> entry : fds.entrySet())
				executor.submit(new ExtensionTaskGeneral(entry.getKey(), entry.getValue(), lhsTrees, this.numAttributes));
		
		// Wait until all FDs are extended
		executor.shutdown();
		try {
			executor.awaitTermination(365, TimeUnit.DAYS);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println((System.currentTimeMillis() - time) + " ms");
	}
	
	private abstract class ExtensionTask implements Runnable {
		protected final BitSet lhs;
		protected final BitSet rhs;
		protected final LhsTree[] lhsTrees;
		protected final int numAttributes;
		public ExtensionTask(final BitSet lhs, final BitSet rhs, final LhsTree[] lhsTrees, final int numAttributes) {
			this.lhs = lhs;
			this.rhs = rhs;
			this.lhsTrees = lhsTrees;
			this.numAttributes = numAttributes;
		}
	}
	
	private class ExtensionTaskGeneral extends ExtensionTask {
		public ExtensionTaskGeneral(final BitSet lhs, final BitSet rhs, final LhsTree[] lhsTrees, final int numAttributes) {
			super(lhs, rhs, lhsTrees, numAttributes);
		}
		@Override
		public void run() {
			// Add the trivial fds to the current fd's rhs for the closure construction (we need to check against ALL the attributes in this fd to build its closure)
			this.rhs.or(this.lhs);
			
			// Extend rhs
			int lastAddedRhsAttribute = -1;
			int currentRhsAttribute = 0;
			do {
				if (!this.rhs.get(currentRhsAttribute)) {
					if (this.lhsTrees[currentRhsAttribute].containsLhsOrSubset(this.rhs)) {
						this.rhs.set(currentRhsAttribute);
						lastAddedRhsAttribute = currentRhsAttribute;
					}
				}
				currentRhsAttribute = (currentRhsAttribute + 1) % this.numAttributes;
			}
			while ((currentRhsAttribute != lastAddedRhsAttribute) && !((currentRhsAttribute == 0) && (lastAddedRhsAttribute < 0)));
			
			// Remove the trivial fds
			this.rhs.andNot(this.lhs);
		}
	}

	private class ExtensionTaskCompleteMinimal extends ExtensionTask {
		public ExtensionTaskCompleteMinimal(final BitSet lhs, final BitSet rhs, final LhsTree[] lhsTrees, final int numAttributes) {
			super(lhs, rhs, lhsTrees, numAttributes);
		}
		@Override
		public void run() {
			BitSet missingLhsAttributes = new BitSet(this.numAttributes);
			missingLhsAttributes.set(0, this.numAttributes);
			missingLhsAttributes.andNot(this.lhs);
			missingLhsAttributes.andNot(this.rhs);
						
			for (int attr = missingLhsAttributes.nextSetBit(0); attr >= 0; attr = missingLhsAttributes.nextSetBit(attr + 1))
				if (this.lhsTrees[attr].containsLhsOrSubset(this.lhs))
					this.rhs.set(attr);
		}
	}

}
