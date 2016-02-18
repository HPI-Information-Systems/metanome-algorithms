package de.hpi.metanome.algorithms.hyfd;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;

import de.hpi.metanome.algorithms.hyfd.structures.FDTree;

public class Inductor {

	private FDTree posCover;
	private MemoryGuardian memoryGuardian;

	public Inductor(FDTree posCover, MemoryGuardian memoryGuardian) {
		this.posCover = posCover;
		this.memoryGuardian = memoryGuardian;
	}

	public void updatePositiveCover(List<OpenBitSet> nonFds) {
		if (nonFds.isEmpty())
			return;
		
		// Sort the negative cover
		System.out.println("Sorting FD-violations ...");
		Collections.sort(nonFds, new Comparator<OpenBitSet>() {
			@Override
			public int compare(OpenBitSet o1, OpenBitSet o2) {
				return (int)(o1.cardinality() - o2.cardinality());
			}
		});
		
		System.out.println("Inducing FD candidates ...");
		for (int i = nonFds.size() - 1; i >= 0; i--) {
			OpenBitSet lhs = nonFds.remove(i);
			
			OpenBitSet fullRhs = lhs.clone();
			fullRhs.flip(0, fullRhs.size());
			
			for (int rhs = fullRhs.nextSetBit(0); rhs >= 0; rhs = fullRhs.nextSetBit(rhs + 1)) {
				int newFDs = this.specializePositiveCover(this.posCover, lhs, rhs);
				this.memoryGuardian.memoryChanged(newFDs);
			}
			
			// If dynamic memory management is enabled, frequently check the memory consumption and trim the positive cover if it does not fit anymore
			this.memoryGuardian.match(this.posCover);
		}
	}
	
	protected int specializePositiveCover(FDTree posCoverTree, OpenBitSet lhs, int rhs) {
		int numAttributes = this.posCover.getChildren().length;
		int newFDs = 0;
		List<OpenBitSet> specLhss = posCoverTree.getFdAndGeneralizations(lhs, rhs);
		for (OpenBitSet specLhs : specLhss) {
			posCoverTree.removeFunctionalDependency(specLhs, rhs);
			
			if (specLhs.cardinality() == posCoverTree.getMaxDepth())
				continue;
			
			for (int attr = numAttributes - 1; attr >= 0; attr--) { // TODO: Is iterating backwards a good or bad idea?
				if (!lhs.get(attr) && (attr != rhs)) {
					specLhs.set(attr);
					if (!posCoverTree.containsFdOrGeneralization(specLhs, rhs)) {
						posCoverTree.addFunctionalDependency(specLhs, rhs);
						newFDs++;					
					}
					specLhs.clear(attr);
				}
			}
		}
		return newFDs;
	}
}
