package de.metanome.algorithms.tireless.regularexpression.matcherclasses;

import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpression;

public class PseudoMatcher extends RegularExpressionMatcher {

    RegularExpression left;
    RegularExpression right;

    public PseudoMatcher(RegularExpression left, RegularExpression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals() {
        return false;
    }

    @Override
    public RegularExpression mergeExpressions() {
        return null;
    }

    @Override
    public RegularExpression getLeft() {
        return left;
    }

    @Override
    public RegularExpression getRight() {
        return right;
    }
}
