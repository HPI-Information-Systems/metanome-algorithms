package de.metanome.algorithms.tireless.preprocessing.alphabet;

import de.metanome.algorithms.tireless.preprocessing.CharClasses;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class DefaultAlphabetTest {

    @Test
    public void testAlphabetContent() {
        AlphabetNode alphabet = makeAlphabet(false);
        BitSet representing = getRepresentation();

        assertEquals(4, alphabet.getDepth());
        assertEquals(representing, alphabet.getRepresentingBitset());
        assertEquals(0, alphabet.getLevel());
    }

    @Test
    public void testAlphabetStructure() {
        AlphabetNode alphabet = makeAlphabet(false);
        List<Alphabet> level1 = alphabet.getSubclasses();

        Set<BitSet> expected = new HashSet<>() {{
            add(new BitSet() {{
                set('ä');
                set('Ä');
                set('a', 'z' + 1);
                set('A', 'Z' + 1);
                set('0', '9' + 1);
            }});
            add(new BitSet() {{
                set('.');
            }});
            add(new BitSet() {{
                set('#');
            }});
        }};

        Set<BitSet> actual = new HashSet<>();

        assertEquals(3, level1.size());
        for (Alphabet child : level1) {
            assertEquals(1, child.getLevel());
            actual.add(child.getRepresentingBitset());
        }
        assertEquals(expected, actual);
    }

    @Test
    public void testExcludedSpecials() {
        AlphabetNode alphabet = makeAlphabet(true);
        BitSet representing = getRepresentation();
        representing.set('#', false);

        assertEquals(representing, alphabet.getRepresentingBitset());
        assertEquals(2, alphabet.getSubclasses().size());
    }

    private AlphabetNode makeAlphabet(boolean excludeSpecial) {
        List<String> values = new ArrayList<>() {{
            add("äÄ0.#");
        }};

        BitSet excluded = new BitSet();
        if (excludeSpecial) excluded.set('#');

        CharClasses charClasses = new CharClasses(values);
        return DefaultAlphabet.getDefaultAlphabet(excluded, charClasses);
    }

    private BitSet getRepresentation() {
        return new BitSet() {{
            set('ä');
            set('Ä');
            set('a', 'z' + 1);
            set('A', 'Z' + 1);
            set('0', '9' + 1);
            set('.');
            set('#');
        }};
    }
}
