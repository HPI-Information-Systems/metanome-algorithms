package de.metanome.algorithms.cfdfinder.pruning;

import de.metanome.algorithms.cfdfinder.pattern.PatternTableau;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.Collection;

public class RhsFilterPruning extends SupportIndependentPruning {

    private IntSet possibleRhs;

    public RhsFilterPruning(int patternThreshold, double minSupportGain, double maxLevelSupportDrop, int possibleRhs) {
        super(patternThreshold, minSupportGain, maxLevelSupportDrop, 1.0);
        this.possibleRhs = new IntAVLTreeSet();
        this.possibleRhs.add(possibleRhs);
    }

    public RhsFilterPruning(int patternThreshold, double minSupportGain, double maxLevelSupportDrop, Collection<Integer> possibleRhs) {
        super(patternThreshold, minSupportGain, maxLevelSupportDrop, 1.0);
        this.possibleRhs = new IntAVLTreeSet(possibleRhs);
    }

    public RhsFilterPruning(int patternThreshold, double minSupportGain, double maxLevelSupportDrop, int possibleRhs, double minCOnfidence) {
        super(patternThreshold, minSupportGain, maxLevelSupportDrop, minCOnfidence);
        this.possibleRhs = new IntAVLTreeSet();
        this.possibleRhs.add(possibleRhs);
    }

    public RhsFilterPruning(int patternThreshold, double minSupportGain, double maxLevelSupportDrop, Collection<Integer> possibleRhs, double minConfidence) {
        super(patternThreshold, minSupportGain, maxLevelSupportDrop, minConfidence);
        this.possibleRhs = new IntAVLTreeSet(possibleRhs);
    }

    public static String getIdentifier() {
        return "RhsFilter";
    }

    @Override
    public boolean continueGeneration(PatternTableau currentTableau) {
        return this.possibleRhs.contains(this.currentCandidate.rhs) && super.continueGeneration(currentTableau);
    }
}
