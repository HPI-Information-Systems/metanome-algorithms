package fdiscovery.columns;

import java.util.ArrayList;


public class DifferenceSets extends ArrayList<DifferenceSet> {

	private static final long serialVersionUID = -912405295927054175L;

	public DifferenceSets() {
		super();
	}
	
	public DifferenceSets(AgreeSets agreeSets) {
		for (AgreeSet agreeSet : agreeSets) {
			this.add(new DifferenceSet(agreeSet));
		}
	}
	
	public DifferenceSets[] allModulo(int numberOfColumns) {
		DifferenceSets[] allDifferenceSetsModulo = new DifferenceSets[numberOfColumns];
		for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++) {
			allDifferenceSetsModulo[columnIndex] = this.modulo(columnIndex);
		}
		return allDifferenceSetsModulo;
	}
	
	public DifferenceSets modulo(int columnIndex) {
		DifferenceSets differenceSetsModulo = new DifferenceSets();
		
		for (DifferenceSet differenceSet : this) {
			if (differenceSet.get(columnIndex)) {
				DifferenceSet differenceSetModuloColumn = (DifferenceSet) differenceSet.clone();
				differenceSetModuloColumn.flip(columnIndex);
				differenceSetsModulo.add(differenceSetModuloColumn);
			}
		}
		
		return differenceSetsModulo;
	}
	
	public DifferenceSets removeCovered(int columnIndex) {
		DifferenceSets uncoveredSets = new DifferenceSets();
		
		for (DifferenceSet differenceSet : this) {
			if (!differenceSet.get(columnIndex)) {
				uncoveredSets.add(differenceSet);
			}
		}
		
		return uncoveredSets;
	}
	
	public boolean containsEmptySet() {
		for (DifferenceSet differenceSet : this) {
			if (differenceSet.cardinality() == 0) {
				return true;
			}
		}
		return false;
	}
	
	public boolean maximumSubsetCoversDifferenceSet(Path path) {
		// an path covers a difference set when the path has at least all bits set that are set in the difference sets
		ArrayList<Path> maximalSubsets = path.getMaximalSubsets();
		
		subsets:
			for (Path maximalSubsetPath : maximalSubsets) {
				for (DifferenceSet differenceSet : this) {
					if (ColumnCollection.intersectionCount(differenceSet, maximalSubsetPath) < 1) {
						// the current maximal subset did not cover all difference sets
						continue subsets;
					}
				}
				// the current maximal subset is a cover for the difference sets
				return true;
			}

		return false;
	}
	
	public String toString() {
		StringBuilder outputBuilder = new StringBuilder();
		for (DifferenceSet differenceSet : this) {
			outputBuilder.append(differenceSet.toString());
			outputBuilder.append(",");
		}
		
		return outputBuilder.toString();
	}

}
