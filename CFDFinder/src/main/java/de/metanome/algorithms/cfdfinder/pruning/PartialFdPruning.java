package de.metanome.algorithms.cfdfinder.pruning;

import de.metanome.algorithms.cfdfinder.pattern.Pattern;
import de.metanome.algorithms.cfdfinder.pattern.PatternTableau;
import de.metanome.algorithms.cfdfinder.structures.FDTreeElement;

import java.util.Set;

public class PartialFdPruning implements PruningStrategy {

    private int numRecords = 0;
    private double desiredG1 = 0;
    private int[][] invertedPLIs;
    private int[] rhs;

    public PartialFdPruning(int numRecords, double desiredG1, int[][] invertedPLIs) {
        this.numRecords = numRecords;
        this.desiredG1 = desiredG1;
        this.invertedPLIs = invertedPLIs;
    }

    public static String getIdentifier() {
        return "G1";
    }

    @Override
    public void startNewTableau(FDTreeElement.InternalFunctionalDependency candidate) {
        this.rhs = this.invertedPLIs[candidate.rhs];
    }

    @Override
    public void finishTableau(PatternTableau tableau) {}

    @Override
    public void addPattern(Pattern pattern) {}

    @Override
    public void expandPattern(Pattern pattern) {}

    @Override
    public void processChild(Pattern child) {}

    @Override
    public boolean hasEnoughPatterns(Set<Pattern> tableau) {
        return tableau.size() >= 1;
    }

    @Override
    public boolean isPatternWorthConsidering(Pattern pattern) {
        return false;
    }

    @Override
    public boolean isPatternWorthAdding(Pattern pattern) {
        return (pattern.calculateG1(this.rhs) / (Math.pow(this.numRecords, 2) - this.numRecords)) <= desiredG1;
    }

    @Override
    public boolean validForProcessing(Pattern child) {
        return false;
    }

    @Override
    public boolean continueGeneration(PatternTableau currentTableau) {
        return currentTableau.getPatterns().size() == 1;
    }
}
