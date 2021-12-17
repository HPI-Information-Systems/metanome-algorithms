package de.metanome.algorithms.tireless.algorithm;

import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.preprocessing.CharClasses;
import de.metanome.algorithms.tireless.preprocessing.alphabet.DefaultAlphabet;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.*;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class RecursiveSubgroupDisjunctionAlgorithmTestAlignment {

    @Test
    public void testAlignmentNonOptional() {
        Map<String, Integer> input = getSampleInput();
        RecursiveSubgroupDisjunctionAlgorithm algorithm = new RecursiveSubgroupDisjunctionAlgorithm(null,
                DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(input.keySet())), getDummyConfiguration());
        RegularExpressionConjunction result =
                algorithm.alignExpressions(99, new RegularExpressionDisjunctionOfTokens(input));

        assertEquals(1, result.getMinCount());
        assertEquals(1, result.getMaxCount());

        for (RegularExpression child : result.getChildren()) {
            assert (1 >= child.getMinCount());
            assertEquals(1, child.getMaxCount());
        }

    }

    @Test
    public void testAlignmentOptional() {
        Map<String, Integer> input = getSampleInput();
        RecursiveSubgroupDisjunctionAlgorithm algorithm = new RecursiveSubgroupDisjunctionAlgorithm(null,
                DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(input.keySet())), getDummyConfiguration());
        RegularExpressionDisjunctionOfTokens expression = new RegularExpressionDisjunctionOfTokens(input);
        expression.setMinCount(0);
        RegularExpressionConjunction result =
                algorithm.alignExpressions(99, expression);

        assertEquals(1, result.getMinCount());
        assertEquals(1, result.getMaxCount());

        for (RegularExpression child : result.getChildren()) {
            assertEquals(0, child.getMinCount());
            assertEquals(1, child.getMaxCount());
        }
    }

    @Test
    public void testCharClassGenerationNonOptional() {
        Map<String, Integer> input = getSampleInput();
        RecursiveSubgroupDisjunctionAlgorithm algorithm = new RecursiveSubgroupDisjunctionAlgorithm(null,
                DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(input.keySet())), getDummyConfiguration());
        RegularExpressionConjunction result =
                algorithm.makeCharacterClass(new RegularExpressionDisjunctionOfTokens(input));

        assertEquals(1, result.getLength());
        assertEquals(ExpressionType.CHARACTER_CLASS, result.getChild(0).getExpressionType());
        assertEquals(1, result.getMinCount());
        assertEquals(1, result.getMaxCount());

        RegularExpressionCharacterClass child = (RegularExpressionCharacterClass) result.getChild(0);
        assertEquals(6, child.getMinCount());
        assertEquals(8, child.getMaxCount());
        assertArrayEquals(new char[]{'-', '4', '5', '6', 'd', 'e', 'i', 'k', 'r', 'w', 'z'},
                child.getCharacterAsCharArray());
    }

    @Test
    public void testCharClassGenerationOptional() {
        Map<String, Integer> input = getSampleInput();
        RecursiveSubgroupDisjunctionAlgorithm algorithm = new RecursiveSubgroupDisjunctionAlgorithm(null,
                DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(input.keySet())), getDummyConfiguration());
        RegularExpressionDisjunctionOfTokens expression = new RegularExpressionDisjunctionOfTokens(input);
        expression.setMinCount(0);
        RegularExpressionConjunction result = algorithm.makeCharacterClass(expression);

        assertEquals(1, result.getLength());
        assertEquals(ExpressionType.CHARACTER_CLASS, result.getChild(0).getExpressionType());
        assertEquals(1, result.getMinCount());
        assertEquals(1, result.getMaxCount());

        RegularExpressionCharacterClass child = (RegularExpressionCharacterClass) result.getChild(0);
        assertEquals(0, child.getMinCount());
        assertEquals(8, child.getMaxCount());
        assertArrayEquals(new char[]{'-', '4', '5', '6', 'd', 'e', 'i', 'k', 'r', 'w', 'z'},
                child.getCharacterAsCharArray());
    }

    @Test
    public void testRefineChildrenDoRefinePartially() {
        Map<String, Integer> input = getSampleInput();
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionToken("Test", null, null));
            addChild(new RegularExpressionCharacterClass(new char[]{'1', '2', '3'}));
            addChild(new RegularExpressionDisjunctionOfTokens(input));
        }};
        RecursiveSubgroupDisjunctionAlgorithm algorithm = new RecursiveSubgroupDisjunctionAlgorithm(null,
                DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(input.keySet())), getDummyConfiguration());
        RegularExpressionConjunction result = algorithm.refineChildren(3, conjunction);

        assert (result.getLength() > 3);
        assertEquals(ExpressionType.TOKEN, result.getChild(0).getExpressionType());
        assertEquals(ExpressionType.CHARACTER_CLASS, result.getChild(1).getExpressionType());
    }

    @Test
    public void testRefineChildrenDoNotRefineHighAlphabetLevel() {
        Map<String, Integer> input = getSampleInput();
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionToken("Test", null, null));
            addChild(new RegularExpressionCharacterClass(new char[]{'1', '2', '3'}));
            addChild(new RegularExpressionDisjunctionOfTokens(input));
        }};
        RecursiveSubgroupDisjunctionAlgorithm algorithm = new RecursiveSubgroupDisjunctionAlgorithm(null,
                DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(input.keySet())), getDummyConfiguration());
        RegularExpressionConjunction result = algorithm.refineChildren(99, conjunction);

        assertEquals(3, result.getLength());
        assertEquals(ExpressionType.TOKEN, result.getChild(0).getExpressionType());
        assertEquals(ExpressionType.CHARACTER_CLASS, result.getChild(1).getExpressionType());
        assertEquals(ExpressionType.DISJUNCTION_OF_TOKENS, result.getChild(2).getExpressionType());
    }

    private Map<String, Integer> getSampleInput() {
        return new HashMap<>() {{
            put("kreide", 1);
            put("zwei-456", 1);
        }};
    }


    private AlgorithmConfiguration getDummyConfiguration() {
        return new AlgorithmConfiguration(0, 0,
                0, 99, 0,
                0, 0);
    }
}
