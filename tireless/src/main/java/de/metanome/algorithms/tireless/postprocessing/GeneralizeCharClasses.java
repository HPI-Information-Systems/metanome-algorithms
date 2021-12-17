package de.metanome.algorithms.tireless.postprocessing;

import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.preprocessing.alphabet.Alphabet;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.*;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class GeneralizeCharClasses {

    private final RegularExpressionConjunction expression;
    private final Alphabet alphabet;
    private final Map<Character, Alphabet> charMap;
    private final AlgorithmConfiguration configuration;

    public GeneralizeCharClasses(RegularExpressionConjunction expression, Alphabet alphabet,
                                 AlgorithmConfiguration configuration) {
        this.expression = expression;
        this.alphabet = alphabet;
        this.charMap = alphabet.getCharMap();
        this.configuration = configuration;
    }

    public void generalizeCharacterClasses() {
        for (RegularExpression child : expression.getChildren())
            if (child.getExpressionType() == ExpressionType.CHARACTER_CLASS)
                generalizeCharacterClass((RegularExpressionCharacterClass) child);
    }

    protected void generalizeCharacterClass(RegularExpressionCharacterClass characterClass) {
        BitSet representation = characterClass.getRepresentation();
        Map<Alphabet, Integer> countMap = new HashMap<>();
        int totalCount = collectStatistics(representation, countMap);
        performGeneralization(characterClass, countMap, totalCount);
    }

    protected void performGeneralization(RegularExpressionCharacterClass characterClass, Map<Alphabet, Integer> countMap,
                                       int totalCount) {
        if (totalCount >= 2 * configuration.CHAR_CLASS_GENERALIZATION_THRESHOLD
                || countMap.values().stream().anyMatch(v -> v >= configuration.CHAR_CLASS_GENERALIZATION_THRESHOLD)) {
            if(totalCount != countMap.values().stream().reduce(0, Integer::sum))
                characterClass.setCharacters(alphabet.getRepresentingBitset());
            else{
                findLowestCommonAncestor(characterClass, countMap);
            }
        }
    }

    protected int collectStatistics(BitSet representation, Map<Alphabet, Integer> countMap) {
        int previousChar = -99;
        AtomicBoolean usedRangeBonus = new AtomicBoolean(false);
        int totalCount = 0;
        for (int i = representation.nextSetBit(0); i >= 0; i = representation.nextSetBit(i + 1)) {
            totalCount = processChar(countMap, previousChar, usedRangeBonus, totalCount, i);
            previousChar = i;
        }
        return totalCount;
    }

    protected int processChar(Map<Alphabet, Integer> countMap, int previousChar, AtomicBoolean usedRangeBonus,
                              int totalCount, int currentChar) {
        Alphabet charAlphabet = charMap.get((char) currentChar);
        if (previousChar + 1 == currentChar
                && configuration.charIsInRange((char) currentChar)
                && configuration.charIsInRange((char) previousChar)) {
            totalCount = processAsRange(countMap, usedRangeBonus, totalCount, charAlphabet);
        } else {
            processAsIndividual(countMap, usedRangeBonus, charAlphabet);
            totalCount++;
        }
        return totalCount;
    }

    protected void processAsIndividual(Map<Alphabet, Integer> countMap, AtomicBoolean usedRangeBonus,
                                     Alphabet charAlphabet) {
        usedRangeBonus.set(false);
        if (charAlphabet != null)
            increaseAlphabetValue(countMap, charAlphabet);
    }

    protected int processAsRange(Map<Alphabet, Integer> countMap, AtomicBoolean usedRangeBonus, int totalCount,
                               Alphabet charAlphabet) {
        if (!usedRangeBonus.get()) {
            if (charAlphabet != null)
                increaseAlphabetValue(countMap, charAlphabet);
            totalCount++;
        }
        usedRangeBonus.set(true);
        return totalCount;
    }

    private void increaseAlphabetValue(Map<Alphabet, Integer> countMap, Alphabet charAlphabet) {
        countMap.put(charAlphabet, countMap.getOrDefault(charAlphabet, 0) + 1);
    }

    protected void findLowestCommonAncestor(RegularExpressionCharacterClass characterClass,
                                          Map<Alphabet, Integer> countMap) {
        Alphabet parentCandidate = countMap.keySet().stream().findFirst().orElse(alphabet);
        BitSet allClasses = new BitSet();
        for(Alphabet nodeToAdd: countMap.keySet()) {
            if (parentCandidate.getLevel() > nodeToAdd.getLevel())
                parentCandidate = nodeToAdd;
            allClasses.or(nodeToAdd.getRepresentingBitset());
        }
        parentCandidate = traverseAlphabetToLowesCommonAncestor(parentCandidate, allClasses);
        characterClass.setCharacters(parentCandidate.getRepresentingBitset());
    }

    protected Alphabet traverseAlphabetToLowesCommonAncestor(Alphabet parentCandidate, BitSet allClasses) {
        boolean foundParent = false;
        while(parentCandidate != alphabet && !foundParent) {
            BitSet copy = (BitSet) allClasses.clone();
            BitSet candidate = parentCandidate.getRepresentingBitset();
            copy.and(candidate);
            if(copy.cardinality() != allClasses.cardinality())
                parentCandidate = parentCandidate.getParent();
            else
                foundParent = true;
        }
        return parentCandidate;
    }
}
