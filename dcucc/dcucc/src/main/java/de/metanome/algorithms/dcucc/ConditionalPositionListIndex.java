package de.metanome.algorithms.dcucc;

import de.metanome.algorithm_helper.data_structures.PositionListIndex;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.List;

/**
 * @author Jens Ehrlich
 */
public class ConditionalPositionListIndex extends PositionListIndex {

  public ConditionalPositionListIndex(List<LongArrayList> clusters) {

  }

  /**
   * TODO update Calculates the condition for a {@link de.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination}.
   * this is the {@link de.metanome.algorithm_helper.data_structures.PositionListIndex}
   * of the partial unique and PLICondition is the {@link de.metanome.algorithm_helper.data_structures.PositionListIndex}
   * of the columns that may form the condition.
   *
   * @param PLICondition a {@link de.metanome.algorithm_helper.data_structures.PositionListIndex}
   *                     that forms the condition
   * @return a list of conditions that hold. Each condition is maximal e.g. there exists no superset
   * for the condition. Only on of the condition holds at a time (xor).
   */
/*  public static List<LongArrayList> calculateConditions(PositionListIndex partialUnique,
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

  public static List<LongArrayList> calculateNotConditions(PositionListIndex partialUnique,
                                                           PositionListIndex PLIcondition,
                                                           int frequency, int numberOfTuples) {
    List<LongArrayList> result = new LinkedList<>();
    int clusterSize = numberOfTuples - frequency;
    outer:
    for (LongArrayList cluster : PLIcondition.getClusters()) {
      if (cluster.size() >= clusterSize) {
        continue;
      }
      for (LongArrayList uniqueClusterNumber : partialUnique.getClusters()) {
        boolean intersectedCurrentCluster = false;
        for (long rowCount : uniqueClusterNumber) {
          //TODO peformance: contains on list vs containsKey on hashmap/set
          if (!cluster.contains(rowCount)) {
            if (!intersectedCurrentCluster) {
              intersectedCurrentCluster = true;
            } else {
              continue outer;
            }
          }
        }
      }
      result.add(cluster);
    }
    return result;
  }*/
}


