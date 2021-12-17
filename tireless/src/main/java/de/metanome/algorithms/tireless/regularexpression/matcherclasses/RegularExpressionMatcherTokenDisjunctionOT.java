package de.metanome.algorithms.tireless.regularexpression.matcherclasses;

import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpression;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionDisjunctionOfTokens;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionToken;

public class RegularExpressionMatcherTokenDisjunctionOT extends RegularExpressionMatcher{
    private final RegularExpressionMatcherDisjunctionOTToken matcher;

    public RegularExpressionMatcherTokenDisjunctionOT
            (RegularExpressionToken left, RegularExpressionDisjunctionOfTokens right) {
        matcher = new RegularExpressionMatcherDisjunctionOTToken(right, left);
    }

    @Override
    public boolean equals() {
        return matcher.equals();
    }

    @Override
    public double getSimilarity() {
        return matcher.getSimilarity();
    }

    @Override
    public RegularExpression mergeExpressions() {
        return matcher.mergeExpressions();
    }

    @Override
    public RegularExpression getLeft() {
        return matcher.getRight();
    }

    @Override
    public RegularExpression getRight() {
        return matcher.getLeft();
    }

}
