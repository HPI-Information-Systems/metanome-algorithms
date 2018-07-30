package de.metanome.algorithms.cfdfinder.pattern;

import java.util.Collection;

public class PatternTableau {

    private Collection<Pattern> patterns;
    private int numberOfTuples;

    public PatternTableau(Collection<Pattern> patterns, int numberOfTuples) {
        this.patterns = patterns;
        this.numberOfTuples = numberOfTuples;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("PatternTableau{");
        for (Pattern pattern : patterns) {
            result.append(pattern.toString());
        }
        result.append("}");
        return result.toString();
    }

    public Collection<Pattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(Collection<Pattern> patterns) {
        this.patterns = patterns;
    }

    public int getGlobalCover() {
        int globalCover = 0;
        for (Pattern p : patterns) {
            globalCover += p.getNumCover();
        }
        return globalCover;
    }

    public int getGlobalKeepers() {
        int globalKeepers = 0;
        for (Pattern p : patterns) {
            globalKeepers += p.getNumKeepers();
        }
        return globalKeepers;
    }

    public double getSupport() {
        return (double) getGlobalCover() / numberOfTuples;
    }

    public double getConfidence() {
        return (double) getGlobalKeepers() / getGlobalCover();
    }
}
