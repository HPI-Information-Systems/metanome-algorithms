package de.metanome.algorithms.dcucc;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.AlgorithmExecutionException;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Jens Ehrlich
 */
public class SimpleConditionTraverser implements ConditionLatticeTraverser {

  protected Dcucc algorithm;

  public SimpleConditionTraverser(Dcucc algorithm) {
    this.algorithm = algorithm;
  }

  @Override
  public void iterateConditionLattice(ColumnCombinationBitset partialUnique)
      throws AlgorithmExecutionException {
    Map<ColumnCombinationBitset, PositionListIndex> currentLevel = new HashMap<>();

    //calculate first level - initialisation
    for (ColumnCombinationBitset conditionColumn : this.algorithm.baseColumn) {
      //TODO better way to prune this columns
      if (partialUnique.containsColumn(conditionColumn.getSetBits().get(0))) {
        continue;
      }
      calculateCondition(partialUnique, currentLevel, conditionColumn,
                         this.algorithm.getPLI(conditionColumn));
    }
    //Intentionally nothing is done with the currentLevel as no complex conditions should be traversed
  }

  protected void calculateCondition(ColumnCombinationBitset partialUnique,
                                    Map<ColumnCombinationBitset, PositionListIndex> currentLevel,
                                    ColumnCombinationBitset conditionColumn,
                                    PositionListIndex conditionPLI)
      throws AlgorithmExecutionException {
    List<LongArrayList> unsatisfiedClusters = new LinkedList<>();
    //check which conditions hold
    List<LongArrayList>
        conditions =
        this.calculateConditions(this.algorithm.getPLI(partialUnique),
                                 conditionPLI,
                                 this.algorithm.frequency,
                                 unsatisfiedClusters);
    if (!unsatisfiedClusters.isEmpty()) {
      currentLevel.put(conditionColumn, new PositionListIndex(unsatisfiedClusters));
    }
    ResultSingleton resultSingleton = ResultSingleton.getInstance();
    for (LongArrayList condition : conditions) {
      List<ConditionEntry> conditionEntries = new LinkedList<>();
      conditionEntries.add(new ConditionEntry(conditionColumn, condition));
      resultSingleton.addConditionToResult(partialUnique, conditionEntries);
    }
  }

  public List<LongArrayList> calculateConditions(PositionListIndex partialUnique,
                                                 PositionListIndex PLICondition,
                                                 int frequency,
                                                 List<LongArrayList> unsatisfiedClusters) {
    List<LongArrayList> result = new LinkedList<>();
    Long2LongOpenHashMap uniqueHashMap = partialUnique.asHashMap();
    LongArrayList touchedClusters = new LongArrayList();
    nextCluster:
    for (LongArrayList cluster : PLICondition.getClusters()) {
      if (cluster.size() < frequency) {
        continue;
      }
      int unsatisfactionCount = 0;
      touchedClusters.clear();
      for (long rowNumber : cluster) {
        if (uniqueHashMap.containsKey(rowNumber)) {
          if (touchedClusters.contains(uniqueHashMap.get(rowNumber))) {
            unsatisfactionCount++;
          } else {
            touchedClusters.add(uniqueHashMap.get(rowNumber));
          }
        }
      }
      if (unsatisfactionCount == 0) {
        result.add(cluster);
      } else {
        if ((cluster.size() - unsatisfactionCount) >= frequency) {
          unsatisfiedClusters.add(cluster);
        }
      }
    }
    return result;
  }

  protected Map<ColumnCombinationBitset, PositionListIndex> apprioriGenerate(
      Map<ColumnCombinationBitset, PositionListIndex> previousLevel) {
    Map<ColumnCombinationBitset, PositionListIndex> nextLevel = new HashMap<>();
    int nextLevelCount = -1;
    ColumnCombinationBitset union = new ColumnCombinationBitset();
    for (ColumnCombinationBitset bitset : previousLevel.keySet()) {
      if (nextLevelCount == -1) {
        nextLevelCount = bitset.size() + 1;
      }
      union = bitset.union(union);
    }

    List<ColumnCombinationBitset> nextLevelCandidates;
    Map<ColumnCombinationBitset, Integer> candidateGenerationCount = new HashMap<>();
    for (ColumnCombinationBitset subset : previousLevel.keySet()) {
      nextLevelCandidates = union.getNSubsetColumnCombinationsSupersetOf(subset, nextLevelCount);
      for (ColumnCombinationBitset nextLevelCandidateBitset : nextLevelCandidates) {
        if (candidateGenerationCount.containsKey(nextLevelCandidateBitset)) {
          int count = candidateGenerationCount.get(nextLevelCandidateBitset);
          count++;
          candidateGenerationCount.put(nextLevelCandidateBitset, count);
        } else {
          candidateGenerationCount.put(nextLevelCandidateBitset, 1);
        }
      }
    }

    for (ColumnCombinationBitset candidate : candidateGenerationCount.keySet()) {
      if (candidateGenerationCount.get(candidate) == nextLevelCount) {
        nextLevel.put(candidate, getConditionPLI(candidate, previousLevel));
      }
    }
    return nextLevel;
  }

  protected PositionListIndex getConditionPLI(ColumnCombinationBitset candidate,
                                              Map<ColumnCombinationBitset, PositionListIndex> pliMap) {
    PositionListIndex firstChild = null;
    for (ColumnCombinationBitset subset : candidate.getDirectSubsets()) {
      if (pliMap.containsKey(subset)) {
        if (firstChild == null) {
          firstChild = pliMap.get(subset);
        } else {
          return pliMap.get(subset).intersect(firstChild);
        }
      }
    }
    //should never arrive here
    return null;
  }
}
