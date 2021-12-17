package de.metanome.algorithms.tireless.preprocessing;

import java.util.BitSet;
import java.util.Set;

public class AlgorithmConfiguration {
    public final int MAXIMUM_ELEMENT_COUNT;
    public final double MINIMAL_SPECIAL_CHARACTER_OCCURRENCE;
    public final int DISJUNCTION_MERGING_THRESHOLD;
    public final double MAXIMUM_LENGTH_DEVIATION_FACTOR;
    public final int CHAR_CLASS_GENERALIZATION_THRESHOLD;
    public final int QUANTIFIER_GENERALIZATION_THRESHOLD;
    public final double OUTLIER_THRESHOLD;

    private final BitSet rangeIntervals = new BitSet() {{
        set('a', 'z' + 1);
        set('A', 'Z' + 1);
        set('0', '9' + 1);
    }};

    private final char[] specialChars =
            new char[]{'\\', '.', '$', '^', '|', '*', '+', '?', '{', '[', '(', ')'};

    private final Set<Character> additionalSpecials = Set.of(']', '=', ':', '-');


    public AlgorithmConfiguration(int maximumElementCount, double minimalSpecialCharacterOccurrence,
                                  int disjunctionMergingThreshold, double maximumLengthDeviationFactor,
                                  int charClassGeneralizationThreshold, int quantifierGeneralizationThreshold,
                                  double outlierThreshold) {
        this.MAXIMUM_ELEMENT_COUNT = maximumElementCount;
        this.MINIMAL_SPECIAL_CHARACTER_OCCURRENCE = minimalSpecialCharacterOccurrence;
        this.DISJUNCTION_MERGING_THRESHOLD = disjunctionMergingThreshold;
        this.MAXIMUM_LENGTH_DEVIATION_FACTOR = maximumLengthDeviationFactor;
        this.CHAR_CLASS_GENERALIZATION_THRESHOLD = charClassGeneralizationThreshold;
        this.QUANTIFIER_GENERALIZATION_THRESHOLD = quantifierGeneralizationThreshold;
        this.OUTLIER_THRESHOLD = outlierThreshold;
    }

    public boolean charIsInRange(char character) {
        return rangeIntervals.get(character);
    }

    public char[] getSpecialChars() {
        return specialChars;
    }

    public Set<Character> getAdditionalSpecials() {
        return additionalSpecials;
    }

}
