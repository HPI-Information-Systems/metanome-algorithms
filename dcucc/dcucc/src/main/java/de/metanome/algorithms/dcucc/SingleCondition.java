package de.metanome.algorithms.dcucc;

import it.unimi.dsi.fastutil.longs.Long2FloatArrayMap;
import it.unimi.dsi.fastutil.longs.LongSet;

public class SingleCondition {

  protected boolean isNegated = false;
  protected Long2FloatArrayMap cluster;
  protected boolean or = true;

  public SingleCondition() {
    cluster = new Long2FloatArrayMap();
  }

  public SingleCondition(boolean isNegated) {
    this();
    this.isNegated = isNegated;
  }

  public void addCluster(long clusterNumber, float coverage) {
    cluster.put(clusterNumber, coverage);
  }

  public LongSet getCluster() {
    return cluster.keySet();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SingleCondition that = (SingleCondition) o;

    if (isNegated != that.isNegated) {
      return false;
    }
    if (or != that.or) {
      return false;
    }
    if (cluster != null ? !cluster.equals(that.cluster) : that.cluster != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = (isNegated ? 1 : 0);
    result = 31 * result + (cluster != null ? cluster.hashCode() : 0);
    result = 31 * result + (or ? 1 : 0);
    return result;
  }
}
