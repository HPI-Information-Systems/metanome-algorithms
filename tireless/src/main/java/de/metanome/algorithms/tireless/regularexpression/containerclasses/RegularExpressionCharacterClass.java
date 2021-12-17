package de.metanome.algorithms.tireless.regularexpression.containerclasses;

import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.preprocessing.alphabet.Alphabet;
import de.metanome.algorithms.tireless.preprocessing.alphabet.AlphabetNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RegularExpressionCharacterClass extends RegularExpression {

    public RegularExpressionCharacterClass() {
        super();
        super.expressionType = ExpressionType.CHARACTER_CLASS;
    }

    public RegularExpressionCharacterClass(char character) {
        this();
        representation.set(character);
    }

    public RegularExpressionCharacterClass(char[] characterGroup) {
        this();
        for (char character : characterGroup)
            representation.set(character);
    }

    public RegularExpressionCharacterClass(BitSet bitSet) {
        super();
        super.expressionType = ExpressionType.CHARACTER_CLASS;
        representation = bitSet;
    }

    @Override
    public int getRepresentationMinCount() {
        return getMinCount();
    }

    @Override
    public int getRepresentationMaxCount() {
        return getMaxCount();
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public int getElementCount(AlgorithmConfiguration configuration, Alphabet alphabet) {
        StringBuilder classString = new StringBuilder();
        List<String> groups = new ArrayList<>();
        AtomicInteger result = new AtomicInteger(0);
        processElements(classString, groups, result, configuration, alphabet);
        return result.get();
    }

    public void setCharacters(BitSet characters) {
        this.representation = characters;
    }

    public char[] getCharacterAsCharArray() {
        char[] result = new char[representation.cardinality()];
        int j = 0;
        for (int i = representation.nextSetBit(0); i >= 0; i = representation.nextSetBit(i + 1))
            result[j++] = (char) i;
        return result;
    }

    @Override
    protected String getCompilation(boolean hasQuantifier, AlgorithmConfiguration configuration, Alphabet alphabet) {
        StringBuilder classString = new StringBuilder();
        List<String> groups = new ArrayList<>();
        processElements(classString, groups, new AtomicInteger(), configuration, alphabet);
        return (classString.length() > 1 || (groups.size() > 0 && !Objects.equals(groups.get(0), "."))) ?
                String.format("[%s%s]", String.join("", groups), classString) : classString.toString();
    }

    private void processElements(StringBuilder classString, List<String> groups, AtomicInteger elementCount,
                                 AlgorithmConfiguration configuration, Alphabet alphabet) {
        BitSet visualize = (BitSet) representation.clone();
        getGroups(visualize, alphabet, groups);
        elementCount.addAndGet(groups.size());
        if (groups.size() == 1 && groups.get(0).equals(".")){
            classString.append(".");
            return;
        }
        boolean inRange = false;
        for (int i = visualize.nextSetBit(0); i >= 0; i = visualize.nextSetBit(i + 1)) {
            inRange = processChar(classString, configuration, visualize, inRange, i, elementCount);
        }
    }

    private boolean processChar(StringBuilder classString, AlgorithmConfiguration configuration,
                                BitSet visualize, boolean inRange, int i, AtomicInteger elementCount) {
        if (inRange)
            if (visualize.get(i + 1) && configuration.charIsInRange((char) (i + 1)))
                return inRange;
            else {
                classString.append("-");
                inRange = false;
            }
        else if (configuration.charIsInRange((char) i) && visualize.get(i + 1) && visualize.get(i + 2)
                && configuration.charIsInRange((char) (i + 1)) && configuration.charIsInRange((char) (i + 2)))
            inRange = true;

        classString.append(escapeIfNecessary((char) i, configuration));
        elementCount.incrementAndGet();
        return inRange;
    }

    private void getGroups(BitSet visualize, Alphabet currentAlphabet, List<String> groups) {
        BitSet copy = (BitSet) currentAlphabet.getRepresentingBitset().clone();
        int initialCardinality = copy.cardinality();
        copy.and(visualize);
        if (copy.cardinality() == initialCardinality) {
            String stringRepresentation = currentAlphabet.getRepresentingString();
            if (!stringRepresentation.isEmpty()) {
                visualize.xor(copy);
                groups.add(stringRepresentation);
            }
        } else {
            if (!currentAlphabet.isLeaf())
                for (Alphabet child : ((AlphabetNode) currentAlphabet).getSubclasses())
                    getGroups(visualize, child, groups);
        }
    }

    private String escapeIfNecessary(char character, AlgorithmConfiguration configuration) {
        if(configuration.getAdditionalSpecials().contains(character)) return "\\" + character;
        else return escapeSpecialCharacters(String.valueOf(character), configuration);
    }
}
