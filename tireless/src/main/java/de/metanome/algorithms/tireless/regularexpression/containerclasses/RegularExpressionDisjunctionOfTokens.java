package de.metanome.algorithms.tireless.regularexpression.containerclasses;

import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.preprocessing.alphabet.Alphabet;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class RegularExpressionDisjunctionOfTokens extends RegularExpression {

    private final Map<String, Integer> children;
    private final Map<Alphabet, Integer> mainAlphabets;
    private final Map<Character, Integer> nonSpecialCharacters;

    public RegularExpressionDisjunctionOfTokens(Map<String, Integer> children) {
        super();
        this.expressionType = ExpressionType.DISJUNCTION_OF_TOKENS;
        this.children = children;
        this.children.remove(null);
        this.children.remove("");
        setAppearanceCount(this.children.values().stream().reduce(0, Integer::sum));
        this.children.keySet().forEach(child -> {
            for (char c : child.toCharArray()) representation.set(c);
        });
        mainAlphabets = new HashMap<>();
        nonSpecialCharacters = new HashMap<>();
    }

    public Map<String, Integer> getChildren() {
        return children;
    }

    public void addAppearanceCountAlphabet(Alphabet alphabet, int count) {
        mainAlphabets.put(alphabet, mainAlphabets.getOrDefault(alphabet, 0) + count);
    }

    public void addAppearanceCountCharacter(Character character, int count) {
        nonSpecialCharacters.put(character, nonSpecialCharacters.getOrDefault(character, 0) + count);
    }

    public Map<Character, Integer> getNonSpecialCharacters() {
        return nonSpecialCharacters;
    }

    public Map<Alphabet, Integer> getMainAlphabets() {
        return mainAlphabets;
    }

    public void addChild(RegularExpressionToken child) {
        children.put(child.getToken(),
                children.getOrDefault(child.getToken(), 0) + child.getAppearanceCount());
        representation.or(child.representation);
    }

    public void removeFromRepresentation(BitSet bitSet) {
        representation.andNot(bitSet);
    }

    public void removeFromRepresentation(Character character) {
        representation.set((int) character, false);
    }

    @Override
    protected String getCompilation(boolean hasQuantifier, AlgorithmConfiguration configuration, Alphabet alphabet) {
        String disjunction = String.join("|", children.keySet().stream()
                .map(c -> escapeSpecialCharacters(c, configuration)).toList());
        return (children.size() > 1 || hasQuantifier) ? String.format("(%s)", disjunction) : disjunction;
    }

    @Override
    public int getRepresentationMinCount() {
        return getMinCount() *
                children.keySet().stream().mapToInt(String::length).min().orElse(0);
    }

    @Override
    public int getRepresentationMaxCount() {
        return getMaxCount() *
                children.keySet().stream().mapToInt(String::length).max().orElse(1);
    }

    @Override
    public int getLength() {
        return children.size();
    }

    @Override
    public int getElementCount(AlgorithmConfiguration configuration, Alphabet alphabet) {
        return children.size();
    }
}
