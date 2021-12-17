package de.metanome.algorithms.tireless.regularexpression.matcherclasses;

import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpression;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionCharacterClass;

import java.util.BitSet;

public class RegularExpressionMatcherClassClass extends RegularExpressionMatcher {

    private final RegularExpressionCharacterClass left;
    private final RegularExpressionCharacterClass right;

    public RegularExpressionMatcherClassClass
            (RegularExpressionCharacterClass left, RegularExpressionCharacterClass right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals() {
        return left.getRepresentation().equals(right.getRepresentation());
    }

    @Override
    public RegularExpression mergeExpressions() {
        BitSet charClass = new BitSet() {{
            or(left.getRepresentation());
            or(right.getRepresentation());
        }};
        return new RegularExpressionCharacterClass(charClass) {{
            setMinCount(Math.min(left.getMinCount(), right.getMinCount()));
            setMaxCount(Math.max(left.getMaxCount(), right.getMaxCount()));
            setAppearanceCount(left.getAppearanceCount() + right.getAppearanceCount());
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

    private boolean leftContainsRight(RegularExpressionCharacterClass left, RegularExpressionCharacterClass right) {
        BitSet rightBitSet = right.getRepresentation();
        BitSet leftBitSet = left.getRepresentation();
        for (int i = rightBitSet.nextSetBit(0); i >= 0; i = rightBitSet.nextSetBit(i + 1)) {
            if (!leftBitSet.get(i))
                return false;
        }
        return true;
    }
}
