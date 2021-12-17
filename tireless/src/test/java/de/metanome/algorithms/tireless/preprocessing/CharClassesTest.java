package de.metanome.algorithms.tireless.preprocessing;

import org.junit.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CharClassesTest {

    @Test
    public void testSimpleValues() {
        List<String> values = new ArrayList<>() {{
            add("2-11-aA");
            add("?a");
        }};
        BitSet expectedLowerCase = new BitSet() {{
            set('a', 'z' + 1);
        }};
        BitSet expectedUpperCase = new BitSet() {{
            set('A', 'Z' + 1);
        }};
        BitSet expectedDigit = new BitSet() {{
            set('0', '9' + 1);
        }};
        BitSet expectedSpecials = new BitSet() {{
            set('-');
            set('?');
        }};
        BitSet expectedOthers = new BitSet();

        CharClasses charClasses = new CharClasses(values);

        assertClassesAreEqual(expectedLowerCase, expectedUpperCase, expectedDigit, expectedSpecials,
                expectedOthers, charClasses);
    }

    @Test
    public void testUndefined() {
        List<String> values = new ArrayList<>() {{
            add("2\u0000\r"); //Null character is special
            add("\u02BC"); //Modifier letter ʼ is letter but not upper or lower case
            add("⛵\u0379"); //⛵ is no letter, 0379 is undefined
        }};

        BitSet expectedLowerCase = new BitSet() {{
            set('a', 'z' + 1);
        }};
        BitSet expectedUpperCase = new BitSet() {{
            set('A', 'Z' + 1);
        }};
        BitSet expectedDigit = new BitSet() {{
            set('0', '9' + 1);
        }};
        BitSet expectedSpecials = new BitSet() {{
            set('\u0000');
            set('\r');
            set('\u26F5');
        }};
        BitSet expectedOthers = new BitSet() {{
            set('\u02BC');
        }};

        CharClasses charClasses = new CharClasses(values);

        assertClassesAreEqual(expectedLowerCase, expectedUpperCase, expectedDigit, expectedSpecials,
                expectedOthers, charClasses);
    }

    private void assertClassesAreEqual(BitSet expectedLowerCase, BitSet expectedUpperCase, BitSet expectedDigit,
                                       BitSet expectedSpecials, BitSet expectedOthers, CharClasses charClasses) {
        assertEquals(expectedLowerCase, charClasses.getLowerCaseClass());
        assertEquals(expectedUpperCase, charClasses.getUpperCaseClass());
        assertEquals(expectedDigit, charClasses.getDigitClass());
        assertEquals(expectedSpecials, charClasses.getSpecialCharClass());
        assertEquals(expectedOthers, charClasses.getOtherLetters());
    }
}
