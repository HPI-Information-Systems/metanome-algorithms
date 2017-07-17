package de.metanome.algorithms.dcucc;

import de.metanome.algorithm_helper.data_structures.PositionListIndex;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jens Ehrlich
 */
public class ConditionalPositionListIndexFixture {

  public int getFrequency() {
    return 3;
  }

  public PositionListIndex getUniquePLIForConditionTest() {
    List<LongArrayList> clusters = new ArrayList<>();
    long[] cluster1 = {1, 2};
    clusters.add(new LongArrayList(cluster1));
    long[] cluster2 = {5, 6};
    clusters.add(new LongArrayList(cluster2));
    long[] cluster3 = {10, 11, 12};
    clusters.add(new LongArrayList(cluster3));
    long[] cluster4 = {13, 14};
    clusters.add(new LongArrayList(cluster4));
    return new PositionListIndex(clusters);
  }

  public PositionListIndex getConditionPLIForConditionTest() {
    List<LongArrayList> clusters = new ArrayList<>();
    long[] cluster1 = {2, 3, 7};
    clusters.add(new LongArrayList(cluster1));
    long[] cluster2 = {4, 5};
    clusters.add(new LongArrayList(cluster2));

    long[] cluster3 = {6, 9, 10};
    clusters.add(new LongArrayList(cluster3));

    long[] cluster4 = {13, 14, 15, 16};
    clusters.add(new LongArrayList(cluster4));
    return new PositionListIndex(clusters);
  }

  public List<LongArrayList> getExpectedConditions() {
    List<LongArrayList> conditions = new ArrayList<>();
    long[] condition1 = {2, 3, 7};
    long[] condition2 = {6, 9, 10};
    conditions.add(new LongArrayList(condition1));
    conditions.add(new LongArrayList(condition2));

    return conditions;
  }

  public List<LongArrayList> getExpectedUnsatisfiedClusters() {
    List<LongArrayList> unsatisfied = new LinkedList<>();
    unsatisfied.add(getConditionPLIForConditionTest().getClusters().get(3));
    return unsatisfied;
  }

  public PositionListIndex getUniquePLIForNotConditionTest() {
    List<LongArrayList> clusters = new ArrayList<>();
    long[] cluster1 = {1, 2};
    clusters.add(new LongArrayList(cluster1));
    long[] cluster2 = {5, 6};
    clusters.add(new LongArrayList(cluster2));

    return new PositionListIndex(clusters);
  }

  public PositionListIndex getConditionPLIForNotConditionTest() {
    List<LongArrayList> clusters = new ArrayList<>();
    long[] cluster1 = {2, 3, 5, 6};
    clusters.add(new LongArrayList(cluster1));

    return new PositionListIndex(clusters);
  }

  public List<LongArrayList> getExpectedNotConditions() {
    List<LongArrayList> conditions = new ArrayList<>();
    long[] condition1 = {2, 3, 5, 6};
    conditions.add(new LongArrayList(condition1));

    return conditions;
  }

  public PositionListIndex getUniquePLIForNotConditionEmptyTest() {
    List<LongArrayList> clusters = new ArrayList<>();
    long[] cluster1 = {1, 2};
    clusters.add(new LongArrayList(cluster1));

    return new PositionListIndex(clusters);
  }

  public PositionListIndex getConditionPLIForNotConditionEmptyTest() {
    List<LongArrayList> clusters = new ArrayList<>();
    long[] cluster1 = {1, 2, 3, 5, 6};
    clusters.add(new LongArrayList(cluster1));

    return new PositionListIndex(clusters);
  }

  public int getNumberOfTuplesEmptyTest() {
    return 8;
  }
}
