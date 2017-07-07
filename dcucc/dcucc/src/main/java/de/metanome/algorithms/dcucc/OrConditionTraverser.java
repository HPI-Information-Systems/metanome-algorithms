package de.metanome.algorithms.dcucc;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.AlgorithmExecutionException;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jens Ehrlich
 */
public class OrConditionTraverser extends SimpleConditionTraverser {

  Map<ColumnCombinationBitset, List<ConditionEntry>> singleConditions;

  public OrConditionTraverser(Dcucc algorithm) {
    super(algorithm);
  }

  @Override
  public void iterateConditionLattice(ColumnCombinationBitset partialUnique)
      throws AlgorithmExecutionException {
    singleConditions = new HashMap<>();
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
    combineClusterIntoResult(partialUnique);
  }

  @Override
  protected void calculateCondition(ColumnCombinationBitset partialUnique,
                                    Map<ColumnCombinationBitset, PositionListIndex> currentLevel,
                                    ColumnCombinationBitset conditionColumn,
                                    PositionListIndex conditionPLI) throws
                                                                    AlgorithmExecutionException {
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

    List<ConditionEntry> clusters = new LinkedList<>();
    for (LongArrayList cluster : conditions) {
      clusters
          .add(new ConditionEntry(conditionColumn, cluster));
    }

    if (clusters.isEmpty()) {
      return;
    }
    for (ColumnCombinationBitset singeConditionColumn : conditionColumn
        .getContainedOneColumnCombinations()) {
      setConditionEntry(singeConditionColumn, clusters);
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
        //if ((cluster.size() - unsatisfactionCount) >= frequency) {
        unsatisfiedClusters.add(cluster);
        //}
      }
    }
    return result;
  }

  protected Long2ObjectOpenHashMap<LongArrayList> purgeIntersectingClusterEntries(
      Long2ObjectOpenHashMap<LongArrayList> result) {
    Long2ObjectOpenHashMap<LongArrayList> purgedResult = new Long2ObjectOpenHashMap<>();
    Iterator<Long> resultIterator = result.keySet().iterator();
    while (resultIterator.hasNext()) {
      long uniqueCluster = resultIterator.next();
      if (result.get(uniqueCluster).size() != 1) {
        purgedResult.put(uniqueCluster, result.get(uniqueCluster));
      }
    }
    return purgedResult;
  }

  protected Set<ColumnCombinationBitset> getConditionStartPoints() {
    return this.singleConditions.keySet();
  }

  protected void combineClusterIntoResult(ColumnCombinationBitset partialUnique)
      throws AlgorithmExecutionException {
    LongArrayList touchedCluster = new LongArrayList();
    Long2LongOpenHashMap partialUniqueHash = this.algorithm.getPLI(partialUnique).asHashMap();
    Set<ColumnCombinationBitset> startPoints = this.getConditionStartPoints();
    for (ColumnCombinationBitset minimalConditionStartPoint : startPoints) {
      if (minimalConditionStartPoint.getSetBits().size() != 1) {
        minimalConditionStartPoint =
            minimalConditionStartPoint.getContainedOneColumnCombinations().get(0);
      }

      List<ConditionEntry> satisfiedCluster = new ArrayList<>();
      Long2ObjectOpenHashMap<LongArrayList> intersectingCluster = new Long2ObjectOpenHashMap<>();
      int clusterNumber = 0;
      //build intersecting cluster
      for (ConditionEntry singleCluster : this.singleConditions.get(minimalConditionStartPoint)) {
        satisfiedCluster.add(singleCluster.setClusterNumber(clusterNumber));
        touchedCluster.clear();
        for (long rowNumber : singleCluster.cluster) {
          if (partialUniqueHash.containsKey(rowNumber)) {
            touchedCluster.add(partialUniqueHash.get(rowNumber));
          }
        }
        for (long partialUniqueClusterNumber : touchedCluster) {
          if (intersectingCluster.containsKey(partialUniqueClusterNumber)) {
            intersectingCluster.get(partialUniqueClusterNumber).add(clusterNumber);
          } else {
            LongArrayList newConditionClusterNumbers = new LongArrayList();
            newConditionClusterNumbers.add(clusterNumber);
            intersectingCluster.put(partialUniqueClusterNumber, newConditionClusterNumbers);
          }
        }
        clusterNumber++;
      }
      intersectingCluster = purgeIntersectingClusterEntries(intersectingCluster);
      //convert into list
      List<LongArrayList> intersectingClusterList = new ArrayList<>();
      for (long partialUniqueCluster : intersectingCluster.keySet()) {
        intersectingClusterList.add(intersectingCluster.get(partialUniqueCluster));
      }

      Object2FloatArrayMap<List<ConditionEntry>>
          clustergroups =
          this.combineClusters(this.algorithm.frequency, satisfiedCluster,
                               intersectingClusterList);

      for (List<ConditionEntry> singleCondition : clustergroups.keySet()) {
        ResultSingleton.getInstance().addConditionToResult(partialUnique, singleCondition,
                                                           clustergroups.get(singleCondition));
      }
    }
  }


  protected Object2FloatArrayMap<List<ConditionEntry>> combineClusters(int frequency,
                                                       List<ConditionEntry> satisfiedClusters,
                                                       List<LongArrayList> intersectingClusters) {
    Object2FloatArrayMap<List<ConditionEntry>> result = new Object2FloatArrayMap();
    //Map<List<ConditionEntry>, float> result = new LinkedList<>();
    LinkedList<ConditionTask> queue = new LinkedList();
    LongArrayList satisfiedClusterNumbers = new LongArrayList();
    Long2LongOpenHashMap totalSizeMap = new Long2LongOpenHashMap();
    long totalSize = 0;
    int i = 0;
    for (ConditionEntry clusters : satisfiedClusters) {
      //satisfiedClusterNumbers.add(conditionMap.get(clusters.get(0)));
      satisfiedClusterNumbers.add(i);
      i++;
      if (clusters.condition.getSetBits().size() < 2) {
        totalSize = totalSize + clusters.cluster.size();
      } else {
        for (long row : clusters.cluster) {
          if (totalSizeMap.containsKey(row)) {
            long previousValue = totalSizeMap.get(row);
            previousValue++;
            totalSizeMap.put(row, previousValue);
          } else {
            totalSizeMap.put(row, 1);
          }
        }
      }
    }
//    totalSize = totalSize + totalSizeMap.size();
    if ((totalSize + totalSizeMap.size()) < frequency) {
      return result;
    }

    ConditionTask
        firstTask =
        new ConditionTask(0, satisfiedClusterNumbers, new LongArrayList(), totalSize, frequency,
                          totalSizeMap);
    queue.add(firstTask);

    while (!queue.isEmpty()) {
      ConditionTask currentTask = queue.remove();
      //finished cluster iterate -> return result
      if (currentTask.uniqueClusterNumber >= intersectingClusters.size()) {
        List<ConditionEntry> validCondition = new LinkedList<>();
        for (long conditionClusterNumber : currentTask.conditionClusters) {
          validCondition.add(satisfiedClusters.get((int) conditionClusterNumber));
        }
        result.put(validCondition, (currentTask.getCoverage() * 100) / Dcucc.numberOfTuples);
        continue;
      }

      int numberOfIntersects = 0;
      for (long cluster : intersectingClusters.get(currentTask.uniqueClusterNumber)) {
        if (currentTask.conditionClusters.contains(cluster)) {
          numberOfIntersects++;
        }
      }

      if (1 >= numberOfIntersects) {
        ConditionTask newTask = currentTask.generateNextTask();
        queue.add(newTask);
        continue;
      }

      //remove at least one cluster for the current intersecting (unique cluster number) cluster
//      for (long conditionCluster : currentTask.conditionClusters) {
      for (long conditionCluster : intersectingClusters.get(currentTask.uniqueClusterNumber)) {
        if (intersectingClusters.get(currentTask.uniqueClusterNumber).contains(conditionCluster)) {
          ConditionTask newTask = currentTask.generateNextTask();
          boolean fullfillsFrequency = true;
          //remove all other clusters
          for (long clusterItem : intersectingClusters.get(currentTask.uniqueClusterNumber)) {
            if (clusterItem == conditionCluster) {
              continue;
            }
            ConditionEntry currentEntryToRemoved = satisfiedClusters.get((int) clusterItem);
            if (!newTask
                .remove(clusterItem, currentEntryToRemoved)) {
              fullfillsFrequency = false;
              break;
            }
          }
          if (fullfillsFrequency) {
            queue.add(newTask);
          }
        }
      }
      //no cluster is removed... because all relevant cluster where removed before -> generate the same task
//      for (long removedConditionCluster : currentTask.removedConditionClusters) {
//        if (intersectingClusters.get((currentTask.uniqueClusterNumber))
//            .contains(removedConditionCluster)) {
//          ConditionTask newTask = currentTask.generateNextTask();
//          queue.add(newTask);
//          break;
//        }
//      }
    }
    return result;
  }

  protected void setConditionEntry(ColumnCombinationBitset singleConditionColumn,
                                   List<ConditionEntry> conditions) {
    List<ConditionEntry> existingCluster;
    if (singleConditions.containsKey(singleConditionColumn)) {
      existingCluster = singleConditions.get(singleConditionColumn);
    } else {
      existingCluster = new LinkedList<>();
      singleConditions.put(singleConditionColumn, existingCluster);
    }
    existingCluster.addAll(conditions);

  }

  protected class ConditionTask {

    protected int uniqueClusterNumber;
    protected LongArrayList conditionClusters;
    protected LongArrayList removedConditionClusters;
    protected long frequency;
    protected Long2LongOpenHashMap andJointCluster;
    private long size = -1;

    public ConditionTask(int uniqueCluster, LongArrayList conditionClusters,
                         LongArrayList removedClusters, long size, long frequency,
                         Long2LongOpenHashMap andJointCluster) {
      this.uniqueClusterNumber = uniqueCluster;
      this.conditionClusters = conditionClusters.clone();
      this.removedConditionClusters = removedClusters.clone();
      this.size = size;
      this.frequency = frequency;
      this.andJointCluster = andJointCluster;
    }

    public ConditionTask generateNextTask() {
      ConditionTask
          newTask =
          new ConditionTask(this.uniqueClusterNumber + 1, this.conditionClusters,
                            this.removedConditionClusters, this.size, this.frequency,
                            this.andJointCluster);
      return newTask;
    }

    public long getCoverage() {
      return this.size + this.andJointCluster.size();
    }

    public boolean remove(long conditionClusterNumber, ConditionEntry entryToRemove) {
      if (entryToRemove.condition.getSetBits().size() < 2) {
        if (((this.size - entryToRemove.cluster.size()) + this.andJointCluster.size())
            >= this.frequency) {
          this.size = this.size - entryToRemove.cluster.size();
          this.conditionClusters.remove(conditionClusterNumber);
          this.removedConditionClusters.add(conditionClusterNumber);
          return true;
        } else {
          return false;
        }
      } else {
        Long2LongOpenHashMap newAndJointCluster = andJointCluster.clone();
        for (long row : entryToRemove.cluster) {
          if (newAndJointCluster.containsKey(row)) {
            long previousValue = newAndJointCluster.get(row);
            previousValue--;
            if (0 == previousValue) {
              newAndJointCluster.remove(row);
            } else {
              newAndJointCluster.put(row, previousValue);
            }
          } else {
            //dunno
          }
        }
        if (this.size + newAndJointCluster.size() >= this.frequency) {
          this.andJointCluster = newAndJointCluster;
          this.conditionClusters.remove(conditionClusterNumber);
          this.removedConditionClusters.add(conditionClusterNumber);
          return true;
        } else {
          return false;
        }
      }
    }
  }
}
