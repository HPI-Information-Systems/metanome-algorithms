package de.metanome.algorithms.dcucc;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_helper.data_structures.SubSetGraph;
import de.metanome.algorithm_integration.AlgorithmExecutionException;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Jens Ehrlich
 */
public class AndConditionTraverser extends SimpleConditionTraverser {

  //partial unique -> basic column -> clusters (one element each)
  protected Map<ColumnCombinationBitset, Map<ColumnCombinationBitset, LongArrayList>>
      clusterPruningMap;
  protected SubSetGraph subetSetGraph;
  public AndConditionTraverser(Dcucc algorithm) {
    super(algorithm);
    this.clusterPruningMap = new HashMap<>();
    this.subetSetGraph = new SubSetGraph();
  }

  @Override
  public void iterateConditionLattice(ColumnCombinationBitset partialUnique)
      throws AlgorithmExecutionException {
    Map<ColumnCombinationBitset, PositionListIndex> currentLevel = new HashMap<>();
    boolean prunePartialUnique = true;
    //calculate first level - initialisation
    for (ColumnCombinationBitset conditionColumn : this.algorithm.baseColumn) {
      //TODO better way to prune this columns
      if (partialUnique.containsColumn(conditionColumn.getSetBits().get(0))) {
        continue;
      }
      PositionListIndex currentConditionPLI = this.algorithm.getPLI(conditionColumn);

      List<ColumnCombinationBitset> traversedSubsets = subetSetGraph.getExistingSubsets(partialUnique);
      List<LongArrayList> clusters = currentConditionPLI.clone().getClusters();
      Iterator<LongArrayList> iterator = clusters.iterator();
      outer:
      while (iterator.hasNext()) {
        LongArrayList currentCluster = iterator.next();
        for (ColumnCombinationBitset traversedSubset : traversedSubsets) {
          LongArrayList prunedClusters = this.clusterPruningMap.get(traversedSubset).get(conditionColumn);
          if (prunedClusters == null) {
            continue;
          }
          for (long cluster : prunedClusters) {
            if (currentCluster.contains(cluster))
              iterator.remove();
              continue outer;
          }
        }
      }
      if (clusters.isEmpty()) {
        continue;
      }
      currentConditionPLI = new PositionListIndex(clusters);
      prunePartialUnique = false;



      calculateCondition(partialUnique, currentLevel, conditionColumn,
                         currentConditionPLI);
    }

    if (prunePartialUnique) {
      this.algorithm.upperPruningGraph.add(partialUnique);
      System.out.println("########## Pruned a node because all clusters are empty");
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
  }

  @Override
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
      resultSingleton.addMinimalConditionToResult(partialUnique, conditionEntries);


      Map<ColumnCombinationBitset, LongArrayList> currentMap = clusterPruningMap.get(partialUnique);
      if (currentMap == null) {
        currentMap = new HashMap<>();
        clusterPruningMap.put(partialUnique, currentMap);
        subetSetGraph.add(partialUnique);
      }
      for (ColumnCombinationBitset oneColumnCondition : conditionColumn.getContainedOneColumnCombinations()) {
        LongArrayList currentList = currentMap.get(oneColumnCondition);
        if (currentList == null) {
          currentList = new LongArrayList();
          currentList.add(condition.get(0));
          currentMap.put(oneColumnCondition, currentList);
        }
        currentList.add(condition.get(0));
      }
    }
  }
}
