package de.metanome.algorithms.tireless.regularexpression.matcherclasses;

import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpression;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionDisjunctionOfTokens;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionToken;

public class RegularExpressionMatcherDisjunctionOTToken extends RegularExpressionMatcher{

    private final RegularExpressionDisjunctionOfTokens left;
    private final RegularExpressionToken right;

    public RegularExpressionMatcherDisjunctionOTToken
            (RegularExpressionDisjunctionOfTokens left, RegularExpressionToken right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals() {
        return left.getLength() == 1 && left.getChildren().containsKey(right.getToken());
    }

    @Override
    public RegularExpression mergeExpressions() {
        left.addChild(right);
        left.setMinCount(Math.min(left.getMinCount(), right.getMinCount()));
        left.setMaxCount(Math.max(left.getMaxCount(), right.getMaxCount()));
        left.setAppearanceCount(left.getAppearanceCount() + right.getAppearanceCount());
        left.addAppearanceCountAlphabet(right.getMainAlphabet(), right.getAppearanceCount());
        for(Character character: right.getNonSpecials())
            left.addAppearanceCountCharacter(character, right.getAppearanceCount());
        return left;
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
