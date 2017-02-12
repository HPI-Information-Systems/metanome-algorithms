package de.metanome.algorithms.hyucc;

import java.util.List;

import org.apache.lucene.util.OpenBitSet;

import de.metanome.algorithms.hyucc.structures.UCCList;
import de.metanome.algorithms.hyucc.structures.UCCSet;
import de.metanome.algorithms.hyucc.structures.UCCTree;
import de.metanome.algorithms.hyucc.utils.Logger;

public class Inductor {

	private UCCSet negCover;
	private UCCTree posCover;
	private MemoryGuardian memoryGuardian;

	public Inductor(UCCSet negCover, UCCTree posCover, MemoryGuardian memoryGuardian) {
		this.negCover = negCover;
		this.posCover = posCover;
		this.memoryGuardian = memoryGuardian;
	}

	public void updatePositiveCover(UCCList nonUCCs) {
		// Sort the negative cover
/*		Logger.getInstance().writeln("Sorting UCC-violations ...");
		Collections.sort(nonUCCs, new Comparator<OpenBitSet>() {
			@Override
			public int compare(OpenBitSet o1, OpenBitSet o2) {
				return (int)(o1.cardinality() - o2.cardinality());
			}
		});
*/		// THE SORTING IS NOT NEEDED AS THE UCCSet SORTS THE NONUCCS BY LEVEL ALREADY
		
		Logger.getInstance().writeln("Inducing UCC candidates ...");
		for (int i = nonUCCs.getUccLevels().size() - 1; i >= 0; i--) {
			if (i >= nonUCCs.getUccLevels().size()) // If this level has been trimmed during iteration
				continue;
			
			List<OpenBitSet> nonUCCLevel = nonUCCs.getUccLevels().get(i);
			for (OpenBitSet nonUCC : nonUCCLevel)
				this.specializePositiveCover(nonUCC, nonUCCs);
			nonUCCLevel.clear();
		}
	}
	
	protected int specializePositiveCover(OpenBitSet nonUCC, UCCList nonUCCs) {
		int numAttributes = this.posCover.getChildren().length;
		int newUCCs = 0;
		List<OpenBitSet> specUCCs;
		
		if (!(specUCCs = this.posCover.getUCCAndGeneralizations(nonUCC)).isEmpty()) { // TODO: May be "while" instead of "if"?
			for (OpenBitSet specUCC : specUCCs) {
				this.posCover.removeUniqueColumnCombination(specUCC);
				
				if ((this.posCover.getMaxDepth() > 0) && (specUCC.cardinality() >= this.posCover.getMaxDepth()))
					continue;
				
				for (int attr = numAttributes - 1; attr >= 0; attr--) {
					if (!nonUCC.get(attr)) {
						specUCC.set(attr);
						if (!this.posCover.containsUCCOrGeneralization(specUCC)) {
							this.posCover.addUniqueColumnCombination(specUCC);
							newUCCs++;	
							
							// If dynamic memory management is enabled, frequently check the memory consumption and trim the positive cover if it does not fit anymore
							this.memoryGuardian.memoryChanged(1);
							this.memoryGuardian.match(this.negCover, this.posCover, nonUCCs);
						}
						specUCC.clear(attr);
					}
				}
			}
		}
		return newUCCs;
	}
}
