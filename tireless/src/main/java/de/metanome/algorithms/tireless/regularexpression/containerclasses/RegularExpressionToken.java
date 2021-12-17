package de.metanome.algorithms.tireless.regularexpression.containerclasses;

import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.preprocessing.alphabet.Alphabet;

import java.util.Set;

public class RegularExpressionToken extends RegularExpression {

    private String token;
    private final Alphabet mainAlphabet;
    private final Set<Character> nonSpecials;

    public RegularExpressionToken(String token, Alphabet mainAlphabet, Set<Character> nonSpecials) {
        super();
        this.token = token;
        super.expressionType = ExpressionType.TOKEN;
        this.mainAlphabet = mainAlphabet;
        this.nonSpecials = nonSpecials;
        for (char character : token.toCharArray())
            representation.set(character);
    }

    public RegularExpressionToken(char[] token, Alphabet mainAlphabet, Set<Character> nonSpecials) {
        this(new String(token), mainAlphabet, nonSpecials);
    }

    public String getToken() {
        return token;
    }

    public Alphabet getMainAlphabet() {
        return mainAlphabet;
    }

    public Set<Character> getNonSpecials() {
        return nonSpecials;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public int getLength() {
        return token.length();
    }

    @Override
    public int getElementCount(AlgorithmConfiguration configuration, Alphabet alphabet) {
        return 1;
    }

    @Override
    public int getRepresentationMinCount() {
        return getMinCount() * token.length();
    }

    @Override
    public int getRepresentationMaxCount() {
        return getMaxCount() * token.length();
    }

    @Override
    protected String getCompilation(boolean hasQuantifier, AlgorithmConfiguration configuration, Alphabet alphabet) {
        String escaped = escapeSpecialCharacters(token, configuration);
        return (token.length() > 1 && hasQuantifier) ? String.format("(%s)", escaped) : escaped;
    }

}
