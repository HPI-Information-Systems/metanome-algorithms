package de.metanome.algorithms.tireless.regularexpression.matcherclasses;

import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpression;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionDisjunctionOfTokens;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionToken;

import java.util.HashMap;

public class RegularExpressionMatcherTokenToken extends RegularExpressionMatcher {

    private final RegularExpressionToken left;
    private final RegularExpressionToken right;

    public RegularExpressionMatcherTokenToken(RegularExpressionToken left, RegularExpressionToken right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals() {
        return left.getToken().equals(right.getToken());
    }

    @Override
    public RegularExpression mergeExpressions() {
        if (equals()) {
            RegularExpression newExpression = (RegularExpression) left.cloneRegex();
            newExpression.setMinCount(Math.min(left.getMinCount(), right.getMinCount()));
            newExpression.setMaxCount(Math.max(left.getMaxCount(), right.getMaxCount()));
            newExpression.setAppearanceCount(left.getAppearanceCount() + right.getAppearanceCount());
            return newExpression;
        } else
            return mergeMismatch();
    }

    private RegularExpressionDisjunctionOfTokens mergeMismatch() {
        HashMap<String, Integer> values = new HashMap<>() {{
            put(left.getToken(), left.getAppearanceCount());
            put(right.getToken(), right.getAppearanceCount());
        }};
        return new RegularExpressionDisjunctionOfTokens(values) {{
            setMinCount(Math.min(left.getMinCount(), right.getMinCount()));
            setMaxCount(Math.max(left.getMaxCount(), right.getMaxCount()));
            addAppearanceCountAlphabet(left.getMainAlphabet(), left.getAppearanceCount());
            addAppearanceCountAlphabet(right.getMainAlphabet(), right.getAppearanceCount());
            for(Character character: left.getNonSpecials())
                addAppearanceCountCharacter(character, left.getAppearanceCount());
            for(Character character: right.getNonSpecials())
                addAppearanceCountCharacter(character, right.getAppearanceCount());
        }};
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
