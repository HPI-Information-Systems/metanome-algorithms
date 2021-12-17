package de.metanome.algorithms.tireless.algorithm;

import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.preprocessing.alphabet.AlphabetNode;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionCharacterClass;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionConjunction;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionDisjunctionOfTokens;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionToken;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class RecursiveSubgroupDisjunctionAlgorithmTestLengthDeviation {

    @Test
    public void testLengthDeviationExceedsThresholdMinimum() {
        testMinMax(2, 4, 1.9, true);
    }

    @Test
    public void testLengthDeviationExceedsThresholdMaximum() {
        testMinMax(4, 16, 1.9, true);
    }

    @Test
    public void testLengthDeviationExceedsThresholdBoth() {
        testMinMax(2, 16, 1.9, true);
    }

    @Test
    public void testLengthDeviationDontExceedsThreshold() {
        testMinMax(2,16,2, false);
    }

    private void testMinMax(final int minCount, final int maxCount, double maximumLengthDeviationFactor,
                            boolean expected) {
        HashMap<String, Integer> values = new HashMap<>() {{
            put("aaaaaaaa", 1);
            put("aaaa", 1);
        }};
        RegularExpressionDisjunctionOfTokens left = new RegularExpressionDisjunctionOfTokens(values);
        RegularExpressionConjunction right = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass() {{
                setMinCount(minCount);
                setMaxCount(maxCount);
            }});
        }};

        RecursiveSubgroupDisjunctionAlgorithm algorithm = new RecursiveSubgroupDisjunctionAlgorithm(null,
                new AlphabetNode(""), new AlgorithmConfiguration(0,
                0, 0, maximumLengthDeviationFactor,
                0, 0, 0));

        assertEquals(expected, algorithm.lengthDeviationExceedsThreshold(left, right));
    }
}
