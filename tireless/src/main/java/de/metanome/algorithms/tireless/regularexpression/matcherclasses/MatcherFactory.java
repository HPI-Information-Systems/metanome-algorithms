package de.metanome.algorithms.tireless.regularexpression.matcherclasses;

import de.metanome.algorithms.tireless.regularexpression.containerclasses.*;
import org.apache.commons.lang3.NotImplementedException;

public class MatcherFactory {

    private final boolean returnPseudoMatcher;

    public MatcherFactory() {
        this(false);
    }

    public MatcherFactory(boolean returnPseudoMatcher) {
        this.returnPseudoMatcher = returnPseudoMatcher;
    }

    public RegularExpressionMatcher getMatcher(RegularExpression left, RegularExpression right) {
        return switch (left.getExpressionType()) {
            case TOKEN -> getTokenMatcher(left, right);
            case DISJUNCTION_OF_TOKENS -> getDisjunctionOTMatcher(left, right);
            case CHARACTER_CLASS -> getCharClassMatcher(left, right);
            default -> throwError(left, right);
        };
    }

    private RegularExpressionMatcher getTokenMatcher(RegularExpression left, RegularExpression right) {
        return switch (right.getExpressionType()) {
            case TOKEN -> new RegularExpressionMatcherTokenToken(
                    (RegularExpressionToken) left, (RegularExpressionToken) right);
            case DISJUNCTION_OF_TOKENS -> new RegularExpressionMatcherTokenDisjunctionOT(
                    (RegularExpressionToken) left, (RegularExpressionDisjunctionOfTokens) right);
            default -> throwError(left, right);
        };
    }

    private RegularExpressionMatcher getDisjunctionOTMatcher(RegularExpression left, RegularExpression right) {
        return switch (right.getExpressionType()) {
            case TOKEN -> new RegularExpressionMatcherDisjunctionOTToken(
                    (RegularExpressionDisjunctionOfTokens) left, (RegularExpressionToken) right);
            default -> throwError(left, right);
        };
    }

    private RegularExpressionMatcher getCharClassMatcher(RegularExpression left, RegularExpression right) {
        return switch (right.getExpressionType()) {
            case CHARACTER_CLASS -> new RegularExpressionMatcherClassClass(
                    (RegularExpressionCharacterClass) left, (RegularExpressionCharacterClass) right);
            default -> throwError(left, right);
        };
    }

    private RegularExpressionMatcher throwError(RegularExpression left, RegularExpression right) {
        if (returnPseudoMatcher)
            return new PseudoMatcher(left, right);
        else
            throw new NotImplementedException(
                    String.format(
                            "No matcher found for left type %s and right type %s. Maybe try exchanging both values",
                            left.getExpressionType(), right.getExpressionType()
                    )
            );
    }


}
