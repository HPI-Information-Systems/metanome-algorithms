package de.metanome.algorithms.tireless.regularexpression.containerclasses;

import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.preprocessing.alphabet.Alphabet;

import java.util.*;

public class RegularExpressionConjunction extends RegularExpression {

    protected List<RegularExpression> children;

    public RegularExpressionConjunction() {
        super();
        this.children = new ArrayList<>();
        super.expressionType = ExpressionType.CONJUNCTION;
    }

    public RegularExpressionConjunction(List<RegularExpression> children) {
        super();
        super.expressionType = ExpressionType.CONJUNCTION;
        for (RegularExpression child : children)
            representation.or(child.getRepresentation());
    }

    public RegularExpressionConjunction(RegularExpressionToken token) {
        this(token.getToken());
        setMinCount(token.getMinCount());
        setMaxCount(token.getMaxCount());
    }

    public RegularExpressionConjunction(String token) {
        this(token.toCharArray());
    }

    public RegularExpressionConjunction(char[] token) {
        this();
        if (token.length == 0) return;
        for (char character : token) {
            RegularExpressionCharacterClass newChild = new RegularExpressionCharacterClass(character);
            children.add(newChild);
            representation.set(character);
        }
    }


    public RegularExpressionConjunction(char[] item, Map<Character, Alphabet> characterLookup, int level,
                                        int appearanceCount) {
        this();
        int startIndex = 0, endIndex = 0;
        char previousChar = item[0];
        Alphabet alphabet = null;
        Set<Character> nonSpecials = new HashSet<>();
        for (char itemChar : item) {
            representation.set(itemChar);
            if (isNewCharacterClass(characterLookup, level, previousChar, itemChar)) {
                appendChildIfNotEmpty(item, startIndex, endIndex, alphabet, nonSpecials, appearanceCount);
                alphabet = characterLookup.get(itemChar).getSuperclassOfLevel(level);
                nonSpecials = new HashSet<>();
                startIndex = endIndex++;
            } else
                endIndex++;
            if (characterLookup.get(itemChar) != null) {
                previousChar = itemChar;
                if(alphabet == null)
                    alphabet = characterLookup.get(itemChar).getSuperclassOfLevel(level);
            } else {
                nonSpecials.add(itemChar);
            }
        }
        appendChildIfNotEmpty(item, startIndex, endIndex, alphabet, nonSpecials, appearanceCount);
    }

    @Override
    public int getRepresentationMinCount() {
        return getMinCount() * children.stream().mapToInt(RegularExpression::getRepresentationMinCount).sum();
    }

    @Override
    public int getRepresentationMaxCount() {
        return getMaxCount() * children.stream().mapToInt(RegularExpression::getRepresentationMaxCount).sum();
    }

    private boolean isNewCharacterClass(Map<Character, Alphabet> characterLookup, int level, char previousChar,
                                        char itemChar) {
        return characterLookup.get(itemChar) != null &&
                characterLookup.get(previousChar) != null &&
                characterLookup.get(itemChar).getSuperclassOfLevel(level)
                        != characterLookup.get(previousChar).getSuperclassOfLevel(level);
    }

    private void appendChildIfNotEmpty(char[] item, int startIndex, int endIndex, Alphabet mainAlphabet,
                                       Set<Character> nonSpecials, int appearanceCount) {
        if (endIndex > startIndex) {
            char[] newToken = Arrays.copyOfRange(item, startIndex, endIndex);
            RegularExpressionToken token = new RegularExpressionToken(newToken, mainAlphabet, nonSpecials);
            token.setAppearanceCount(appearanceCount);
            children.add(token);
        }
    }

    @Override
    protected String getCompilation(boolean hasQuantifier, AlgorithmConfiguration configuration, Alphabet alphabet) {
        StringBuilder result = new StringBuilder();
        for (RegularExpression regex : getChildren()) result.append(regex.compile(configuration, alphabet));
        return (hasQuantifier && getLength() > 1) ? String.format("(%s)", result) : result.toString();
    }

    @Override
    public int getLength() {
        return children.size();
    }

    @Override
    public int getElementCount(AlgorithmConfiguration configuration, Alphabet alphabet) {
        int sum = 0;
        for (RegularExpression child : children)
            sum += child.getElementCount(configuration, alphabet);
        return sum;
    }

    public List<RegularExpression> getChildren() {
        return children;
    }

    public RegularExpression getChild(int i) {
        return children.get(i);
    }

    public void addChild(RegularExpression child) {
        children.add(child);
        representation.or(child.getRepresentation());
    }

    public void addChild(RegularExpression child, int index) {
        children.add(index, child);
        representation.or(child.getRepresentation());
    }

    public void addChildren(List<RegularExpression> children) {
        for (RegularExpression child : children) {
            this.children.add(child);
            representation.or(child.getRepresentation());
        }
    }
}
