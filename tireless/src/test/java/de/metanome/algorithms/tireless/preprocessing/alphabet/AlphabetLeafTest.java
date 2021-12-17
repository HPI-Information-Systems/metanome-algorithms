package de.metanome.algorithms.tireless.preprocessing.alphabet;

import org.junit.Test;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AlphabetLeafTest {

    @Test
    public void testResetLevel() {
        AlphabetLeaf leaf = new AlphabetLeaf(null, "");
        assertEquals(0, leaf.getLevel());
        leaf.resetLevel(10);
        assertEquals(10, leaf.getLevel());
    }

    @Test
    public void testGetRepresentingBitset() {
        BitSet sampleBitset = new BitSet() {{
            set('a');
            set('b');
        }};
        AlphabetLeaf leaf = new AlphabetLeaf(sampleBitset, "");

        assertEquals(sampleBitset, leaf.getRepresentingBitset());
    }

    @Test
    public void testGetCharMap() {
        BitSet sampleBitset = new BitSet() {{
            set('a');
            set('b');
        }};
        AlphabetLeaf leaf = new AlphabetLeaf(sampleBitset, "");

        Map<Character, Alphabet> expected = new HashMap<>() {{
            put('a', leaf);
            put('b', leaf);
        }};

        assertEquals(expected, leaf.getCharMap());
    }

    @Test
    public void testDepth() {
        AlphabetLeaf leaf = new AlphabetLeaf(null, "");
        assertEquals(1, leaf.getDepth());
    }
}
