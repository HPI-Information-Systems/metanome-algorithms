package de.metanome.algorithms.cfdfinder.pattern;

import java.util.List;

public class RangePatternEntry extends PatternEntry {

    private List<Integer> sortedClusters;
    private int minCluster, maxCluster;

    public RangePatternEntry(List<Integer> sortedClusters, int minCluster, int maxCluster) {
        this.sortedClusters = sortedClusters;
        this.minCluster = minCluster;
        this.maxCluster = maxCluster;

        if (minCluster < 0 || maxCluster > sortedClusters.size()) {
            throw new IllegalArgumentException("Invalid cluster ids.");
        }
    }

    public RangePatternEntry copy() {
        return new RangePatternEntry(sortedClusters, minCluster, maxCluster);
    }

    public boolean increaseLowerBound() {
        minCluster += 1;
        return minCluster <= maxCluster;
    }

    public boolean decreaseUpperBound() {
        maxCluster -= 1;
        return maxCluster >= minCluster;
    }

    public int getLowerBound() {
        return sortedClusters.get(minCluster).intValue();
    }

    public int getUpperBound() {
        return sortedClusters.get(maxCluster).intValue();
    }

    @Override
    boolean matches(int value) {
        return minCluster <= value && maxCluster >= value;
    }

    @Override
    boolean isVariable() {
        return sortedClusters.size() > 0 && minCluster <= 0 && maxCluster >= sortedClusters.size() - 1;
    }

    @Override
    public String toString() {
        return "RangePatternEntry [" + minCluster + " - " + maxCluster + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RangePatternEntry that = (RangePatternEntry) o;

        if (minCluster != that.minCluster) return false;
        if (maxCluster != that.maxCluster) return false;
        return sortedClusters != null ? sortedClusters.equals(that.sortedClusters) : that.sortedClusters == null;
    }

    @Override
    public int hashCode() {
        int result = sortedClusters != null ? sortedClusters.hashCode() : 0;
        result = 31 * result + minCluster;
        result = 31 * result + maxCluster;
        return result;
    }
}
