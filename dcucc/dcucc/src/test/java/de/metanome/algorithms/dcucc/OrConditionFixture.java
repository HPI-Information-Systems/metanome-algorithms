package de.metanome.algorithms.dcucc;

import de.metanome.algorithm_helper.data_structures.PositionListIndex;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jens Ehrlich
 */
public class OrConditionFixture {

  public PositionListIndex getUniquePLI() {
    List<LongArrayList> clusters = new ArrayList<>();

    long[] cluster1 = {1, 2};
    clusters.add(new LongArrayList(cluster1));
    long[] cluster2 = {5, 6};
    clusters.add(new LongArrayList(cluster2));
    long[] cluster3 = {10, 11, 12};
    clusters.add(new LongArrayList(cluster3));

    return new PositionListIndex(clusters);
  }

  public PositionListIndex getConditionPLI() {
    List<LongArrayList> clusters = new ArrayList<>();

    long[] cluster1 = {2, 3, 7};
    clusters.add(new LongArrayList(cluster1));
    long[] cluster2 = {4, 5};
    clusters.add(new LongArrayList(cluster2));
    long[] cluster3 = {6, 9, 10};
    clusters.add(new LongArrayList(cluster3));

    return new PositionListIndex(clusters);
  }

  public List<LongArrayList> getExpectedConditions() {
    List<LongArrayList> conditions = new ArrayList<>();
    long[] condition1 = {2, 3, 7, 4, 5};
    long[] condition2 = {2, 3, 7, 6, 9, 10};
    conditions.add(new LongArrayList(condition1));
    conditions.add(new LongArrayList(condition2));

    return conditions;
  }

  public int getFrequency() {
    return 3;
  }
}
