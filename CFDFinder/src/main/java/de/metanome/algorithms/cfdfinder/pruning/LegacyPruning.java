package de.metanome.algorithms.cfdfinder.pruning;

import de.metanome.algorithms.cfdfinder.pattern.*;
import de.metanome.algorithms.cfdfinder.structures.FDTreeElement;

import java.util.*;

public class LegacyPruning implements PruningStrategy {

    private double minSupport, minConfidence, cummulativeSupport;
    private int numberOfTuples;
    private Set<Pattern> V;

    public LegacyPruning(double minSupport, double minConfidence, int numberOfTuples) {
        this.minSupport = minSupport;
        this.minConfidence = minConfidence;
        this.numberOfTuples = numberOfTuples;
    }

    public static String getIdentifier() {
        return "Legacy";
    }

    @Override
    public void startNewTableau(FDTreeElement.InternalFunctionalDependency candidate) {
        cummulativeSupport = 0;
        V = new HashSet<>();
    }

    @Override
    public void finishTableau(PatternTableau tableau) {
    }

    @Override
    public void addPattern(Pattern pattern) {
        cummulativeSupport += pattern.getSupport();
    }

    @Override
    public void expandPattern(Pattern pattern) {
        V.add(pattern);
    }

    @Override
    public void processChild(Pattern child) {
    }

    @Override
    public boolean hasEnoughPatterns(Set<Pattern> tableau) {
        return cummulativeSupport >= (minSupport * numberOfTuples);
    }

    @Override
    public boolean isPatternWorthConsidering(Pattern pattern) {
        return pattern.getSupport() > 0;
    }

    @Override
    public boolean isPatternWorthAdding(Pattern pattern) {
        return pattern.getConfidence() >= minConfidence;
    }

    @Override
    public boolean validForProcessing(Pattern child) {
        return V.containsAll(getParentPatterns(child));
    }

    @Override
    public boolean continueGeneration(PatternTableau currentTableau) {
        return currentTableau.getSupport() >= minSupport && currentTableau.getConfidence() >= minConfidence;
    }

    private List<Pattern> getParentPatterns(Pattern c) {
        List<Pattern> results = new LinkedList<>();
        for (int i = 0; i < c.getIds().length; i += 1) {
            int id = c.getIds()[i];
            PatternEntry entry = c.getPatternEntries()[i];
            if (entry instanceof ConstantPatternEntry) {
                HashMap<Integer, PatternEntry> copy = new HashMap<>(c.getAttributes());
                copy.put(Integer.valueOf(id), new VariablePatternEntry());
                results.add(new Pattern(copy));
            }
        }
        return results;
    }
}
