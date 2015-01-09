package fdiscovery.columns;

import fdiscovery.equivalence.TEquivalence;
import fdiscovery.fastfds.EquivalenceClass;
import fdiscovery.fastfds.EquivalenceClasses;
import fdiscovery.fastfds.MaximalEquivalenceClasses;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TIntHashSet;

public class AgreeSets extends THashSet<AgreeSet> {

	private static final long serialVersionUID = -1599769422319745084L;

	public AgreeSets() {
		super();
	}
	
	public AgreeSets(MaximalEquivalenceClasses maximalEquivalenceClasses, EquivalenceClasses equivalenceClasses, int numberOfColumns, int numberOfRows) {
		TIntHashSet[] comparedCouples = new TIntHashSet[numberOfRows];
		for (int i = 0; i < comparedCouples.length; i++) {
			comparedCouples[i] = new TIntHashSet();
		}
		for (TEquivalence equivalenceGroup : maximalEquivalenceClasses) {
			int[] equivalenceGroupArray = equivalenceGroup.toArray();
			// generate all 2-tuples based on the current equivalence class
			for (int outerIndex = 0; outerIndex < equivalenceGroup.size()-1; outerIndex++) {
				for (int innerIndex = outerIndex+1; innerIndex < equivalenceGroup.size(); innerIndex++) {
					
					// make sure that you don't check the same couples more than one time
					// needs too much space for most datasets
//					if (!comparedCouples[outerIndex].contains(innerIndex)) {
//						comparedCouples[outerIndex].add(innerIndex);
//					
						// create a couple consisting of tuple t and tuple t'
						EquivalenceClass equivalenceClassTupleT = equivalenceClasses.get(equivalenceGroupArray[outerIndex]);
						EquivalenceClass equivalenceClassTupleTPrime = equivalenceClasses.get(equivalenceGroupArray[innerIndex]);
						AgreeSet agreeSet = new AgreeSet(equivalenceClassTupleT, equivalenceClassTupleTPrime, numberOfColumns);
						this.add(agreeSet);
//					}
				}
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder outputBuilder = new StringBuilder();
		outputBuilder.append("AgreeSets\n");
		for (AgreeSet agreeSet : this) {
			outputBuilder.append(agreeSet.toString());
			outputBuilder.append(",");
		}
		outputBuilder.append("\n");
		outputBuilder.append("AgreeSets\n");
		
		return outputBuilder.toString();
	}
}
