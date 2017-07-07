package de.metanome.algorithms.dcucc;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;

import it.unimi.dsi.fastutil.longs.LongArrayList;

/**
 * @author Jens Ehrlich
 */
public class ConditionEntry {

  public ColumnCombinationBitset condition;
  public LongArrayList cluster;
  public float coverage;
  public int clusterNumber;

  public ConditionEntry(ColumnCombinationBitset condition, LongArrayList cluster) {
    this.condition = new ColumnCombinationBitset(condition);
    this.cluster = cluster.clone();
    this.coverage = (float) ((cluster.size() * 100.0) / Dcucc.numberOfTuples);
  }

  public ConditionEntry setClusterNumber(int clusterNumber) {
    this.clusterNumber = clusterNumber;
    return this;
  }
}
