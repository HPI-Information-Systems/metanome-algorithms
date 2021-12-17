package de.metanome.algorithms.tireless.postprocessing;

import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.preprocessing.CharClasses;
import de.metanome.algorithms.tireless.preprocessing.alphabet.Alphabet;
import de.metanome.algorithms.tireless.preprocessing.alphabet.DefaultAlphabet;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.ExpressionType;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionCharacterClass;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionConjunction;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;

public class GeneralizeCharClassesTest {

    @Test
    public void testAlphabetTraversalFirstCandidateCorrect() {
        BitSet allClasses = new BitSet() {{
            set('0', '5');
        }};
        Alphabet alphabet = DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(getInput()));
        GeneralizeCharClasses generalize = new GeneralizeCharClasses(null, alphabet, getDummyConfiguration());
        Alphabet result = generalize.traverseAlphabetToLowesCommonAncestor(alphabet.getCharMap().get('0'), allClasses);

        assertEquals(alphabet.getCharMap().get('0'), result);
    }

    @Test
    public void testAlphabetTraversalIntermediate() {
        BitSet allClasses = new BitSet() {{
            set('0', '5');
            set('A', 'E');
        }};
        Alphabet alphabet = DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(getInput()));
        GeneralizeCharClasses generalize = new GeneralizeCharClasses(null, alphabet, getDummyConfiguration());
        Alphabet result = generalize.traverseAlphabetToLowesCommonAncestor(alphabet.getCharMap().get('0'), allClasses);

        assertEquals(alphabet.getCharMap().get('0').getParent(), result);
    }

    @Test
    public void testAlphabetTraversalRoot() {
        BitSet allClasses = new BitSet() {{
            set('0', '5');
            set('A', 'E');
            set('&');
        }};
        Alphabet alphabet = DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(getInput()));
        GeneralizeCharClasses generalize = new GeneralizeCharClasses(null, alphabet, getDummyConfiguration());
        Alphabet result = generalize.traverseAlphabetToLowesCommonAncestor(alphabet.getCharMap().get('0'), allClasses);

        assertEquals(alphabet, result);
    }

    @Test
    public void testAlphabetTraversalUnknownInput() {
        BitSet allClasses = new BitSet() {{
            set('0', '5');
            set('A', 'E');
            set(':');
        }};
        Alphabet alphabet = DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(getInput()));
        GeneralizeCharClasses generalize = new GeneralizeCharClasses(null, alphabet, getDummyConfiguration());
        Alphabet result = generalize.traverseAlphabetToLowesCommonAncestor(alphabet.getCharMap().get('0'), allClasses);

        assertEquals(alphabet, result);
    }

    @Test
    public void testProcessCharIndividualCharAlphabetSet() {
        Alphabet alphabet = DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(getInput()));
        GeneralizeCharClasses generalize = new GeneralizeCharClasses(null, alphabet, getDummyConfiguration());
        Map<Alphabet, Integer> map = new HashMap<>();
        AtomicBoolean rangeBonus = new AtomicBoolean(true);
        int result = generalize.processChar(map, 'A', rangeBonus, 6, 'C');

        assertEquals(7, result);
        assertEquals(1, map.size());
        assertEquals(1, map.get(alphabet.getCharMap().get('A')).intValue());
        assert (!rangeBonus.get());
    }

    @Test
    public void testProcessCharRangeCharAlphabetUnset() {
        Alphabet alphabet = DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(getInput()));
        GeneralizeCharClasses generalize = new GeneralizeCharClasses(null, alphabet, getDummyConfiguration());
        Map<Alphabet, Integer> map = new HashMap<>();
        AtomicBoolean rangeBonus = new AtomicBoolean(true);
        int result = generalize.processChar(map, 'Ö', rangeBonus, 6, 'Ü');

        assertEquals(7, result);
        assertEquals(0, map.size());
        assert (!rangeBonus.get());
    }

    @Test
    public void testProcessCharRangeCharAlphabetSet() {
        Alphabet alphabet = DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(getInput()));
        GeneralizeCharClasses generalize = new GeneralizeCharClasses(null, alphabet, getDummyConfiguration());
        Map<Alphabet, Integer> map = new HashMap<>();
        AtomicBoolean rangeBonus = new AtomicBoolean(false);
        int result = generalize.processChar(map, 'Z', rangeBonus, 6, 'B');
        result = generalize.processChar(map, 'B', rangeBonus, result, 'C');

        assertEquals(8, result);
        assertEquals(1, map.size());
        assertEquals(2, map.get(alphabet.getCharMap().get('A')).intValue());
        assert (rangeBonus.get());
    }

    @Test
    public void testPerformGeneralizationNoGeneralization() {
        Alphabet alphabet = DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(getInput()));
        GeneralizeCharClasses generalize = new GeneralizeCharClasses(null, alphabet, getDummyConfiguration());
        BitSet bitSet = new BitSet() {{
            set('A', 'C');
        }};
        RegularExpressionCharacterClass input = new RegularExpressionCharacterClass((BitSet) bitSet.clone());
        generalize.performGeneralization(input, new HashMap<>(), 1);

        assertEquals(bitSet, input.getRepresentation());
        assertEquals(2, input.getRepresentation().cardinality());
    }

    @Test
    public void testPerformGeneralizationGeneralizeTotalCount() {
        Alphabet alphabet = DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(getInput()));
        GeneralizeCharClasses generalize = new GeneralizeCharClasses(null, alphabet, getDummyConfiguration());
        BitSet bitSet = new BitSet() {{
            set('A', 'C' + 1);
        }};
        BitSet expectedOutput = new BitSet() {{
            set('A', 'Z' + 1);
            set('a', 'z' + 1);
            set('0', '9' + 1);
            set('%');
            set('&');
            set('/');
            set('(');
            set(')');
        }};
        RegularExpressionCharacterClass input = new RegularExpressionCharacterClass((BitSet) bitSet.clone());
        generalize.performGeneralization(input, new HashMap<>(), 6);

        assertEquals(expectedOutput, input.getRepresentation());
    }

    @Test
    public void testPerformGeneralizationGeneralizeCharMap() {
        Alphabet alphabet = DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(getInput()));
        GeneralizeCharClasses generalize = new GeneralizeCharClasses(null, alphabet, getDummyConfiguration());
        BitSet bitSet = new BitSet() {{
            set('A', 'C' + 1);
        }};
        BitSet expectedOutput = new BitSet() {{
            set('A', 'Z' + 1);
        }};
        RegularExpressionCharacterClass input = new RegularExpressionCharacterClass((BitSet) bitSet.clone());
        Map<Alphabet, Integer> map = new HashMap<>() {{
            put(alphabet.getCharMap().get('A'), 4);
        }};
        generalize.performGeneralization(input, map, 4);

        assertEquals(expectedOutput, input.getRepresentation());
    }

    @Test
    public void testEndToEndNoGeneralization() {
        Alphabet alphabet = DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(getInput()));
        BitSet bitSet = new BitSet() {{
            set('A', 'C' + 1);
            set('a', 'c' + 1);
        }};
        RegularExpressionCharacterClass expression = new RegularExpressionCharacterClass((BitSet) bitSet.clone());
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(expression);
        }};
        GeneralizeCharClasses generalize = new GeneralizeCharClasses(conjunction, alphabet, getDummyConfiguration());
        generalize.generalizeCharacterClasses();

        assertEquals(bitSet, conjunction.getChild(0).getRepresentation());
        assertEquals(6, conjunction.getChild(0).getRepresentation().cardinality());
        assertEquals(1, conjunction.getLength());
        assertEquals(ExpressionType.CHARACTER_CLASS, conjunction.getChild(0).getExpressionType());
    }

    @Test
    public void testEndToEndGeneralizationOneClass() {
        Alphabet alphabet = DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(getInput()));
        BitSet bitSet = new BitSet() {{
            set('A', 'C' + 1);
            set('E', 'F' + 1);
            set('1');
        }};
        RegularExpressionCharacterClass expression = new RegularExpressionCharacterClass(bitSet);
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(expression);
        }};
        GeneralizeCharClasses generalize = new GeneralizeCharClasses(conjunction, alphabet, getDummyConfiguration());
        generalize.generalizeCharacterClasses();

        assertEquals(alphabet.getCharMap().get('0').getParent().getRepresentingBitset(),
                conjunction.getChild(0).getRepresentation());
        assertEquals(1, conjunction.getLength());
        assertEquals(ExpressionType.CHARACTER_CLASS, conjunction.getChild(0).getExpressionType());
    }

    @Test
    public void testEndToEndGeneralizationDifferentClasses() {
        Alphabet alphabet = DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(getInput()));
        BitSet bitSet = new BitSet() {{
            set('A', 'C' + 1);
            set('e', 'f' + 1);
            set('1', '2' + 1);
        }};
        RegularExpressionCharacterClass expression = new RegularExpressionCharacterClass(bitSet);
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(expression);
        }};
        GeneralizeCharClasses generalize = new GeneralizeCharClasses(conjunction, alphabet, getDummyConfiguration());
        generalize.generalizeCharacterClasses();

        assertEquals(alphabet.getCharMap().get('0').getParent().getRepresentingBitset(),
                conjunction.getChild(0).getRepresentation());
        assertEquals(1, conjunction.getLength());
        assertEquals(ExpressionType.CHARACTER_CLASS, conjunction.getChild(0).getExpressionType());
    }

    @Test
    public void testEndToEndGeneralizationMultipleRanges() {
        Alphabet alphabet = DefaultAlphabet.getDefaultAlphabet(new BitSet(), new CharClasses(getInput()));
        BitSet bitSet = new BitSet() {{
            set('0', '2' + 1);
            set('4', '9' + 1);
        }};
        RegularExpressionCharacterClass expression = new RegularExpressionCharacterClass(bitSet);
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(expression);
        }};
        GeneralizeCharClasses generalize = new GeneralizeCharClasses(conjunction, alphabet, new AlgorithmConfiguration(
                0,0,0,0,
                4, 0, 0));
        generalize.generalizeCharacterClasses();

        assertEquals(alphabet.getCharMap().get('0').getRepresentingBitset(),
                conjunction.getChild(0).getRepresentation());
        assertEquals(10, conjunction.getChild(0).getRepresentation().cardinality());
        assertEquals(1, conjunction.getLength());
        assertEquals(ExpressionType.CHARACTER_CLASS, conjunction.getChild(0).getExpressionType());
    }

    private List<String> getInput() {
        return new ArrayList<>() {{
            add("0123456789");
            add("abcdef");
            add("ABCDEF");
            add("%&/()");
        }};
    }

    private AlgorithmConfiguration getDummyConfiguration() {
        return new AlgorithmConfiguration(0, 0,
                0, 0, 3,
                0, 0);
    }
}
