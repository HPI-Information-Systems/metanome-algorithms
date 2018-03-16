package de.metanome.algorithms.cfdfinder.pruning;

import de.metanome.algorithms.cfdfinder.pattern.Pattern;
import de.metanome.algorithms.cfdfinder.pattern.PatternTableau;
import de.metanome.algorithms.cfdfinder.structures.FDTreeElement;
import org.apache.lucene.util.OpenBitSet;

import java.util.*;

import static de.metanome.algorithms.cfdfinder.utils.LhsUtils.generateLhsSupersets;

public class SupportIndependentPruning implements PruningStrategy {

    private int patternThreshold;
    private double minSupportGain;
    private double minConfidence;
    private boolean insufficientSupportGain = false;
    protected FDTreeElement.InternalFunctionalDependency currentCandidate;
    private Map<FDTreeElement.InternalFunctionalDependency, Double> supportMap = new HashMap<>();
    private double maxLevelSupportDrop;
    private Set<Pattern> visited;

    public SupportIndependentPruning(int patternThreshold, double minSupportGain, double maxLevelSupportDrop, double minConfidence) {
        this.patternThreshold = patternThreshold;
        this.minSupportGain = minSupportGain;
        this.maxLevelSupportDrop = maxLevelSupportDrop;
        this.minConfidence = minConfidence;

        if (minSupportGain < 1) {
            throw new IllegalArgumentException("minSupportGain has to be specified in tuples (not percent!)");
        } else if (patternThreshold < 1) {
            throw new IllegalArgumentException("patternThreshold needs to be at least 1 to generate any patterns.");
        }
    }

    public static String getIdentifier() {
        return "SupportGainPruning";
    }

    @Override
    public void startNewTableau(FDTreeElement.InternalFunctionalDependency candidate) {
        insufficientSupportGain = false;
        currentCandidate = candidate;
        visited = new HashSet<>();
    }

    @Override
    public void finishTableau(PatternTableau tableau) {
    }

    @Override
    public void addPattern(Pattern pattern) {
        visited.clear();
    }

    @Override
    public void expandPattern(Pattern pattern) {
    }

    @Override
    public void processChild(Pattern child) {
        child.setCover(null);
        visited.add(child);
    }

    @Override
    public boolean hasEnoughPatterns(Set<Pattern> tableau) {
        return (tableau.size() >= patternThreshold) || insufficientSupportGain;
    }

    @Override
    public boolean isPatternWorthConsidering(Pattern pattern) {
        return pattern.getSupport() >= minSupportGain;
    }

    @Override
    public boolean isPatternWorthAdding(Pattern pattern) {
        if (pattern.getSupport() < minSupportGain) {
            insufficientSupportGain = true;
            return false;
        }
        return pattern.getConfidence() >= minConfidence;
    }

    @Override
    public boolean validForProcessing(Pattern child) {
        return !visited.contains(child);
    }

    @Override
    public boolean continueGeneration(PatternTableau currentTableau) {
        if (currentTableau.getPatterns().size() == 0 || currentTableau.getSupport() == 0) {
            return false;
        }
        supportMap.put(currentCandidate, currentTableau.getSupport());
        double maxSupport = 0;
        for (OpenBitSet b : generateLhsSupersets(currentCandidate.lhs)) {
            FDTreeElement.InternalFunctionalDependency parent = new FDTreeElement.InternalFunctionalDependency(b, currentCandidate.rhs);
            if (supportMap.containsKey(parent)) {
                double support = supportMap.get(parent);
                if (support >= maxSupport) {
                    maxSupport = support;
                }
            }
        }
        return maxSupport - maxLevelSupportDrop <= currentTableau.getSupport();
    }
}
