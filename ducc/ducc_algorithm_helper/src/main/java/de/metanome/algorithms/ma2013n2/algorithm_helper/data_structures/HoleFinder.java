package de.metanome.algorithms.ma2013n2.algorithm_helper.data_structures;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HoleFinder {

  protected Set<ColumnCombinationBitset> complementarySet;
  protected ColumnCombinationBitset allBitsSet;

  public HoleFinder(int numberOfColumns) {
    this.allBitsSet = new ColumnCombinationBitset();
    this.allBitsSet.setAllBits(numberOfColumns);
    this.complementarySet = new HashSet<>();
  }

  public List<ColumnCombinationBitset> getHoles() {
    return new ArrayList<>(this.complementarySet);
  }

  public List<ColumnCombinationBitset> getHolesWithoutGivenColumnCombinations(
      Set<ColumnCombinationBitset> columnCombinationSet) {
    List<ColumnCombinationBitset> holes = new ArrayList<>();

    for (ColumnCombinationBitset current : this.complementarySet) {
      if (!columnCombinationSet.contains(current)) {
        holes.add(current);
      }
    }

    return holes;
  }

  public void removeMinimalPositivesFromComplementarySet(List<ColumnCombinationBitset> sets) {
    for (ColumnCombinationBitset singleSet : sets) {
      this.complementarySet.remove(singleSet);
    }
  }

  public void removeMinimalPositiveFromComplementarySet(ColumnCombinationBitset set) {
    this.complementarySet.remove(set);
  }

  public void update(ColumnCombinationBitset maximalNegative) {
    ColumnCombinationBitset singleComplementMaxNegative = this.allBitsSet.minus(maximalNegative);

    if (this.complementarySet.size() == 0) {
      for (ColumnCombinationBitset combination : singleComplementMaxNegative
          .getContainedOneColumnCombinations()) {
        this.complementarySet.add(combination);
      }

      return;
    }

    ArrayList<ColumnCombinationBitset> complementarySetsArray = new ArrayList<>();
    this.addPossibleCombinationsToComplementArray(complementarySetsArray,
                                                  singleComplementMaxNegative);
    this.removeSubsetsFromComplementarySetsArray(complementarySetsArray);

    this.complementarySet.clear();
    for (ColumnCombinationBitset c : complementarySetsArray) {
      if (c != null) {
        this.complementarySet.add(c);
      }
    }
  }

  // TODO: rename to removeSubsetsFromComplementarySets, because Datatype can change, and Array is not even correct now
  protected void removeSubsetsFromComplementarySetsArray(
      List<ColumnCombinationBitset> complementarySetsList) {
    int a, b;
    for (a = 0; a < complementarySetsList.size(); a++) {
      if (complementarySetsList.get(a) == null) {
        continue;
      }

      for (b = 0; b < complementarySetsList.size(); b++) {
        if (a == b || complementarySetsList.get(b) == null) {
          continue;
        }

        if (complementarySetsList.get(b).containsSubset(complementarySetsList.get(a))) {
          complementarySetsList.set(b, null);
        }
      }
    }
  }

  // TODO: rename (Array --> List)
  protected void addPossibleCombinationsToComplementArray(
      List<ColumnCombinationBitset> complementSet, ColumnCombinationBitset singleComplement) {
    ColumnCombinationBitset intersectedCombination;

    List<ColumnCombinationBitset>
        oneColumnBitSetsOfSingleComplement =
        singleComplement.getContainedOneColumnCombinations();

    for (ColumnCombinationBitset set : this.complementarySet) {
      intersectedCombination = set.intersect(singleComplement);

      if (intersectedCombination.getSetBits().size() != 0) {
        complementSet.add(set);
        continue;
      }

      // TODO: maybe optimize union to set single bit
      for (ColumnCombinationBitset oneColumnBitSet : oneColumnBitSetsOfSingleComplement) {
        complementSet.add(set.union(oneColumnBitSet));
      }
    }
  }
}
