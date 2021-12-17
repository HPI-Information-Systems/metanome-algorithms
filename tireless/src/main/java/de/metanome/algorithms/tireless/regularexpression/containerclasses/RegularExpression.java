package de.metanome.algorithms.tireless.regularexpression.containerclasses;

import de.metanome.algorithms.tireless.postprocessing.GeneralizeQuantifiers;
import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.preprocessing.alphabet.Alphabet;

import java.util.BitSet;
import java.util.Objects;

public abstract class RegularExpression implements Cloneable {
    protected ExpressionType expressionType;
    protected BitSet representation;
    private int minCount;
    private int maxCount;
    private int appearanceCount;

    public RegularExpression() {
        this.minCount = 1;
        this.maxCount = 1;
        representation = new BitSet();
        appearanceCount = 1;
    }

    public BitSet getRepresentation() {
        return representation;
    }

    public abstract int getRepresentationMinCount();

    public abstract int getRepresentationMaxCount();

    public Object cloneRegex() {
        try {
            return clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getMinCount() {
        return minCount;
    }

    public void setMinCount(int minCount) {
        this.minCount = minCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public ExpressionType getExpressionType() {
        return expressionType;
    }

    public abstract int getLength();

    public abstract int getElementCount(AlgorithmConfiguration configuration, Alphabet alphabet);

    public String compile(AlgorithmConfiguration configuration, Alphabet alphabet) {
        String quantifier = new GeneralizeQuantifiers(configuration).getQuantifier(this);
        String compiled = getCompilation(!Objects.equals(quantifier, ""), configuration, alphabet);
        return compiled + quantifier;
    }

    protected abstract String getCompilation(boolean hasQuantifier, AlgorithmConfiguration configuration,
                                             Alphabet alphabet);

    protected String escapeSpecialCharacters(String regexToken, AlgorithmConfiguration configuration) {
        for (char toReplace: configuration.getSpecialChars())
            regexToken = regexToken.replace(String.valueOf(toReplace), "\\" + toReplace);
        return  regexToken;
    }


    public int getAppearanceCount() {
        return appearanceCount;
    }

    public void setAppearanceCount(int appearanceCount) {
        this.appearanceCount = appearanceCount;
    }

}
