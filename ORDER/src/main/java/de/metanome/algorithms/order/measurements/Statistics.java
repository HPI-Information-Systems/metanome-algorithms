package de.metanome.algorithms.order.measurements;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class Statistics {

  private String algorithmName;
  private long typeInferralTime;
  private long loadDataTime;
  private long initPartitionsTime;
  private long computeDependenciesTime;
  private long pruneTime;
  private long genNextTime;
  private long numDependencyChecks;
  private final IntList numPartitionCombinationsBySize;

  private int numFoundDependencies;
  private int numFoundDependenciesInPreCheck;

  private final IntList numApplicationsUniquenessPruning;
  private final IntList numApplicationsSwapPruning;
  private final IntList numApplicationsValidRhsPruning;
  private final IntList numApplicationsMinimalityLhsPruning;
  private final IntList numApplicationsMinimalityRhsPruning;
  private final IntList numApplicationsMergePruning;

  private final IntList numApplicationsEquivalenceLhsPruning;
  private final IntList numApplicationsEquivalenceRhsPruning;

  public Statistics(final String algorithmName) {
    this.numApplicationsUniquenessPruning = new IntArrayList();
    this.numApplicationsSwapPruning = new IntArrayList();
    this.numApplicationsValidRhsPruning = new IntArrayList();
    this.numApplicationsMinimalityLhsPruning = new IntArrayList();
    this.numApplicationsMinimalityRhsPruning = new IntArrayList();
    this.numApplicationsMergePruning = new IntArrayList();
    this.numApplicationsEquivalenceLhsPruning = new IntArrayList();
    this.numApplicationsEquivalenceRhsPruning = new IntArrayList();
    this.numPartitionCombinationsBySize = new IntArrayList();

    this.algorithmName = algorithmName;
  }

  private void increaseMeasure(final IntList measureList, final int level) {
    if (measureList.size() > level - 1) {
      final int prev = measureList.getInt(level - 1);
      measureList.set(level - 1, prev + 1);
    } else {
      final int skippedLevels = level - measureList.size() - 1;
      // add 0 for no application of the pruning rule in the skipped levels
      for (int i = 0; i < skippedLevels; i++) {
        measureList.add(0);
      }
      // add 1 for one application of the pruning rule in the new level
      measureList.add(1);
    }
  }

  public void increaseNumPartitionCombinationsBySize(final int size) {
    this.increaseMeasure(this.numPartitionCombinationsBySize, size);
  }

  public void increaseNumApplicationsEquivalenceLhsPruning(final int level) {
    this.increaseMeasure(this.numApplicationsEquivalenceLhsPruning, level);
  }

  public void increaseNumApplicationsEquivalenceRhsPruning(final int level) {
    this.increaseMeasure(this.numApplicationsEquivalenceRhsPruning, level);
  }

  public void increaseNumApplicationsUniquenessPruning(final int level) {
    this.increaseMeasure(this.numApplicationsUniquenessPruning, level);
  }

  @Override
  public String toString() {
    return "Statistics [algorithmName=" + this.algorithmName + ", typeInferralTime="
        + this.typeInferralTime + ", loadDataTime=" + this.loadDataTime + ", initPartitionsTime="
        + this.initPartitionsTime + ", computeDependenciesTime=" + this.computeDependenciesTime
        + ", pruneTime=" + this.pruneTime + ", genNextTime=" + this.genNextTime
        + ", numDependencyChecks=" + this.numDependencyChecks + ", numPartitionCombinationsBySize="
        + this.numPartitionCombinationsBySize + ", numFoundDependencies="
        + this.numFoundDependencies + ", numFoundDependenciesInPreCheck="
        + this.numFoundDependenciesInPreCheck + ", numApplicationsUniquenessPruning="
        + this.numApplicationsUniquenessPruning + ", numApplicationsSwapPruning="
        + this.numApplicationsSwapPruning + ", numApplicationsValidRhsPruning="
        + this.numApplicationsValidRhsPruning + ", numApplicationsMinimalityLhsPruning="
        + this.numApplicationsMinimalityLhsPruning + ", numApplicationsMinimalityRhsPruning="
        + this.numApplicationsMinimalityRhsPruning + ", numApplicationsMergePruning="
        + this.numApplicationsMergePruning + ", numApplicationsEquivalenceLhsPruning="
        + this.numApplicationsEquivalenceLhsPruning + ", numApplicationsEquivalenceRhsPruning="
        + this.numApplicationsEquivalenceRhsPruning + "]";
  }

  public void increaseNumApplicationsSwapPruning(final int level) {
    this.increaseMeasure(this.numApplicationsSwapPruning, level);
  }

  public void increaseNumApplicationsMinimalityLhsPruning(final int level) {
    this.increaseMeasure(this.numApplicationsMinimalityLhsPruning, level);
  }

  public void increaseNumApplicationsMinimalityRhsPruning(final int level) {
    this.increaseMeasure(this.numApplicationsMinimalityRhsPruning, level);
  }

  public void increaseNumApplicationsValidRhsPruning(final int level) {
    this.increaseMeasure(this.numApplicationsValidRhsPruning, level);
  }

  public void increaseNumApplicationsMergePruning(final int level) {
    this.increaseMeasure(this.numApplicationsMergePruning, level);
  }

  public int getNumFoundDependencies() {
    return this.numFoundDependencies;
  }

  public void setNumFoundDependencies(final int numFoundDependencies) {
    this.numFoundDependencies = numFoundDependencies;
  }

  public int getNumFoundDependenciesInPreCheck() {
    return this.numFoundDependenciesInPreCheck;
  }

  public void setNumFoundDependenciesInPreCheck(final int numFoundDependenciesInPreCheck) {
    this.numFoundDependenciesInPreCheck = numFoundDependenciesInPreCheck;
  }

  public long getNumDependencyChecks() {
    return this.numDependencyChecks;
  }

  public void setNumDependencyChecks(final long numDependencyChecks) {
    this.numDependencyChecks = numDependencyChecks;
  }

  public String getAlgorithmName() {
    return this.algorithmName;
  }

  public void setAlgorithmName(final String algorithmName) {
    this.algorithmName = algorithmName;
  }

  public long getTypeInferralTime() {
    return this.typeInferralTime;
  }

  public void setTypeInferralTime(final long typeInferralTime) {
    this.typeInferralTime = typeInferralTime;
  }

  public long getLoadDataTime() {
    return this.loadDataTime;
  }

  public void setLoadDataTime(final long loadDataTime) {
    this.loadDataTime = loadDataTime;
  }

  public long getInitPartitionsTime() {
    return this.initPartitionsTime;
  }

  public void setInitPartitionsTime(final long initPartitionsTime) {
    this.initPartitionsTime = initPartitionsTime;
  }

  public long getComputeDependenciesTime() {
    return this.computeDependenciesTime;
  }

  public void setComputeDependenciesTime(final long computeDependenciesTime) {
    this.computeDependenciesTime = computeDependenciesTime;
  }

  public long getPruneTime() {
    return this.pruneTime;
  }

  public void setPruneTime(final long pruneTime) {
    this.pruneTime = pruneTime;
  }

  public long getGenNextTime() {
    return this.genNextTime;
  }

  public void setGenNextTime(final long genNextTime) {
    this.genNextTime = genNextTime;
  }

}
