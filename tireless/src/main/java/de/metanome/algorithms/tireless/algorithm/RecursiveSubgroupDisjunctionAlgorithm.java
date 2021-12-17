package de.metanome.algorithms.tireless.algorithm;

import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.preprocessing.alphabet.Alphabet;
import de.metanome.algorithms.tireless.regularexpression.RegularExpressionComparator;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class RecursiveSubgroupDisjunctionAlgorithm {

    private final Map<String, Integer> rawContent;
    private final Alphabet alphabet;
    private final Map<Character, Alphabet> charMap;

    private final AlgorithmConfiguration configuration;
    private int minimalOccurrenceThreshold = 0;


    public RecursiveSubgroupDisjunctionAlgorithm(Map<String, Integer> column, Alphabet alphabet,
                                                 AlgorithmConfiguration configuration) {
        rawContent = column;
        this.alphabet = alphabet;
        charMap = alphabet.getCharMap();
        this.configuration = configuration;
    }

    public RegularExpressionConjunction computeExpression() {
        RegularExpressionDisjunctionOfTokens expression = new RegularExpressionDisjunctionOfTokens(rawContent);
        expression.addAppearanceCountAlphabet(alphabet, expression.getAppearanceCount());
        minimalOccurrenceThreshold = (int) Math.floor(configuration.OUTLIER_THRESHOLD * expression.getAppearanceCount());
        if (expression.getLength() == 0) return null;
        return computeExpression(1, expression);
    }

    protected RegularExpressionConjunction computeExpression(int alphabetLevel,
                                                             RegularExpressionDisjunctionOfTokens expression) {
        if (expression.getLength() < configuration.DISJUNCTION_MERGING_THRESHOLD)
            return new RegularExpressionConjunction() {{
                addChild(expression);
            }};
        RegularExpressionConjunction conjunction = alignExpressions(alphabetLevel, expression);
        if (alphabetLevel < alphabet.getDepth()) {
            new OutlierDetection(expression, conjunction, minimalOccurrenceThreshold).detectAndRemoveOutliers();
        }
        if (lengthDeviationExceedsThreshold(expression, conjunction))
            return makeCharacterClass(expression);
        return refineChildren(alphabetLevel, conjunction);
    }

    protected RegularExpressionConjunction refineChildren(int alphabetLevel, RegularExpressionConjunction conjunction) {
        RegularExpressionConjunction result = new RegularExpressionConjunction();
        for (RegularExpression child : conjunction.getChildren()) {
            if (child.getExpressionType() != ExpressionType.DISJUNCTION_OF_TOKENS || alphabetLevel >= alphabet.getDepth())
                result.addChild(child);
            else
                result.addChildren(computeExpression(alphabetLevel + 1,
                        (RegularExpressionDisjunctionOfTokens) child).getChildren());
        }
        return result;
    }

    protected RegularExpressionConjunction makeCharacterClass(RegularExpressionDisjunctionOfTokens expression) {
        RegularExpressionCharacterClass characterClass = new RegularExpressionCharacterClass() {{
            setCharacters(expression.getRepresentation());
            setMinCount(expression.getRepresentationMinCount());
            setMaxCount(expression.getRepresentationMaxCount());
        }};
        return new RegularExpressionConjunction() {{
            addChild(characterClass);
        }};
    }


    protected boolean lengthDeviationExceedsThreshold(RegularExpressionDisjunctionOfTokens disjunctionOT,
                                                      RegularExpressionConjunction conjunction) {
        boolean exceedsMinimum = conjunction.getRepresentationMinCount()
                * configuration.MAXIMUM_LENGTH_DEVIATION_FACTOR
                < disjunctionOT.getRepresentationMinCount();
        boolean exceedsMaximum = conjunction.getRepresentationMaxCount()
                / configuration.MAXIMUM_LENGTH_DEVIATION_FACTOR
                > disjunctionOT.getRepresentationMaxCount();
        return exceedsMinimum || exceedsMaximum;
    }

    protected RegularExpressionConjunction alignExpressions(int alphabetLevel,
                                                            RegularExpressionDisjunctionOfTokens disjunctionOT) {
        List<RegularExpressionConjunction> expressions = splitTokensToConjunction(alphabetLevel, disjunctionOT);
        expressions.sort(new RegularExpressionComparator());
        RegularExpressionConjunction result = runAlignment(expressions);
        for (RegularExpression child : result.getChildren())
            child.setMinCount(child.getMinCount() * disjunctionOT.getMinCount());
        return result;
    }

    protected List<RegularExpressionConjunction> splitTokensToConjunction(
            int alphabetLevel, RegularExpressionDisjunctionOfTokens disjunctionOT) {
        List<RegularExpressionConjunction> expressions = new ArrayList<>();
        for (String token : disjunctionOT.getChildren().keySet()) {
            RegularExpressionConjunction conjunction;
            if (alphabetLevel >= alphabet.getDepth())
                conjunction = new RegularExpressionConjunction(token.toCharArray());
            else
                conjunction = new RegularExpressionConjunction(token.toCharArray(), charMap, alphabetLevel,
                        disjunctionOT.getChildren().get(token));
            expressions.add(conjunction);
        }
        return expressions;
    }

    protected RegularExpressionConjunction runAlignment(List<RegularExpressionConjunction> expressions) {
        AtomicReference<RegularExpressionConjunction> result = new AtomicReference<>(expressions.get(0));
        expressions.stream().skip(1).forEachOrdered(
                value -> result.set(
                        new NeedlemanWunschAlignmentPunishElongation(result.get(), value).mergeExpressions()));
        return result.get();
    }
}
