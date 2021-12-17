package de.metanome.algorithms.tireless.algorithm;

import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.preprocessing.CharClasses;
import de.metanome.algorithms.tireless.preprocessing.alphabet.DefaultAlphabet;
import de.metanome.algorithms.tireless.regularexpression.RegularExpressionComparator;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.*;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class RecursiveSubgroupDisjunctionAlgorithmTestTokenSplitting {

    @Test
    public void splitToTokensLowerLevels() {
        Map<String, Integer> input = new HashMap<>() {{
            put("eins", 1);
            put("zwei", 1);
        }};
        RecursiveSubgroupDisjunctionAlgorithm algorithm = new RecursiveSubgroupDisjunctionAlgorithm(null,
                DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(input.keySet())), getDummyConfiguration());

        List<RegularExpressionConjunction> result =
                algorithm.splitTokensToConjunction(1, new RegularExpressionDisjunctionOfTokens(input));

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getLength());
        assertEquals(1, result.get(1).getLength());

        assertEquals("eins", ((RegularExpressionToken) (result.get(0).getChild(0))).getToken());
        assertEquals("zwei", ((RegularExpressionToken) (result.get(1).getChild(0))).getToken());
    }

    @Test
    public void splitToTokensHighestLevels() {
        Map<String, Integer> input = new HashMap<>() {{
            put("eins", 1);
            put("zwei_drei", 1);
        }};
        RecursiveSubgroupDisjunctionAlgorithm algorithm = new RecursiveSubgroupDisjunctionAlgorithm(null,
                DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(input.keySet())), getDummyConfiguration());

        List<RegularExpressionConjunction> result =
                algorithm.splitTokensToConjunction(99, new RegularExpressionDisjunctionOfTokens(input));

        result.sort(new RegularExpressionComparator());

        assertEquals(2, result.size());
        assertEquals(4, result.get(0).getLength());
        assertEquals(9, result.get(1).getLength());

        for (RegularExpressionConjunction child : result)
            for (RegularExpression subChild : child.getChildren())
                assertEquals(ExpressionType.CHARACTER_CLASS, subChild.getExpressionType());
    }

    private AlgorithmConfiguration getDummyConfiguration() {
        return new AlgorithmConfiguration(0, 0,
                0, 0, 0,
                0, 0);
    }
}
