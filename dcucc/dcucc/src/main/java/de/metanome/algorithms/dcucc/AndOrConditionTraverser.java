package de.metanome.algorithms.dcucc;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_helper.data_structures.SubSetGraph;
import de.metanome.algorithm_integration.AlgorithmExecutionException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jens Ehrlich
 */
public class AndOrConditionTraverser extends OrConditionTraverser {

  protected SubSetGraph foundConditions;

  public AndOrConditionTraverser(Dcucc algorithm) {
    super(algorithm);
  }

  @Override
  public void iterateConditionLattice(ColumnCombinationBitset partialUnique)
      throws AlgorithmExecutionException {
    singleConditions = new HashMap<>();
    foundConditions = new SubSetGraph();
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

    currentLevel = apprioriGenerate(currentLevel);

    Map<ColumnCombinationBitset, PositionListIndex> nextLevel = new HashMap<>();
    while (!currentLevel.isEmpty()) {
      nextLevel.clear();
      for (ColumnCombinationBitset potentialCondition : currentLevel.keySet()) {
        calculateCondition(partialUnique, nextLevel, potentialCondition,
                           currentLevel.get(potentialCondition));
      }
      //TODO what if nextLevel is already empty?
      currentLevel = apprioriGenerate(nextLevel);
    }
    combineClusterIntoResult(partialUnique);
  }

  @Override
  protected void setConditionEntry(ColumnCombinationBitset singleConditionColumn,
                                   List<ConditionEntry> conditions) {
    for (ConditionEntry entry : conditions) {
      this.foundConditions.add(entry.condition);
    }

    List<ConditionEntry> existingCluster;
    if (singleConditions.containsKey(singleConditionColumn)) {
      existingCluster = singleConditions.get(singleConditionColumn);
    } else {
      existingCluster = new LinkedList<>();
      singleConditions.put(singleConditionColumn, existingCluster);
    }
    existingCluster.addAll(conditions);
  }

  @Override
  protected Set<ColumnCombinationBitset> getConditionStartPoints() {
    return foundConditions.getMinimalSubsets();
  }
}