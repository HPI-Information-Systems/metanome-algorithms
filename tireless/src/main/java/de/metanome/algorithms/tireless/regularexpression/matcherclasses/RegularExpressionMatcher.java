package de.metanome.algorithms.tireless.regularexpression.matcherclasses;

import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpression;

import java.util.BitSet;

public abstract class RegularExpressionMatcher {

    public abstract boolean equals();

    public double getSimilarity() {
        RegularExpression left = getLeft();
        RegularExpression right = getRight();
        BitSet intersection = (BitSet) left.getRepresentation().clone();
        intersection.and(right.getRepresentation());
        return -1 + 2 * (double) intersection.cardinality() / (left.getRepresentation().cardinality() +
                right.getRepresentation().cardinality() - intersection.cardinality());
    }

    public abstract RegularExpression mergeExpressions();

    public abstract RegularExpression getLeft();

    public abstract RegularExpression getRight();
}
