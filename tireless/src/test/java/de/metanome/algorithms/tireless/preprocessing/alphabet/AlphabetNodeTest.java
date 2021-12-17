package de.metanome.algorithms.tireless.preprocessing.alphabet;

import org.junit.Test;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AlphabetNodeTest {

    @Test
    public void testRepresentingBitsetNoLeaves() {
        AlphabetNode node1 = new AlphabetNode("");
        AlphabetNode node2 = new AlphabetNode("");
        node1.addSubclass(node2);

        BitSet expected = new BitSet();

        assertEquals(expected, node1.getRepresentingBitset());
    }

    @Test
    public void testRepresentingBitsetWithLeaves() {
        BitSet bitSet1 = new BitSet() {{
            set('a');
            set('b');
        }};
        BitSet bitSet2 = new BitSet() {{
            set('b');
            set('c');
        }};
        AlphabetNode node1 = new AlphabetNode("");
        AlphabetNode node2 = new AlphabetNode("");
        AlphabetLeaf leaf1 = new AlphabetLeaf(bitSet1, "");
        AlphabetLeaf leaf2 = new AlphabetLeaf(bitSet2, "");

        node1.addSubclass(node2);
        node1.addSubclass(leaf1);
        node2.addSubclass(leaf2);

        BitSet expected = new BitSet() {{
            set('a', 'c' + 1);
        }};

        assertEquals(expected, node1.getRepresentingBitset());
    }

    @Test
    public void testResetLevelNoSubclasses() {
        AlphabetNode node = new AlphabetNode("");
        node.resetLevel(-1);

        assertEquals(-1, node.getLevel());
    }

    @Test
    public void testResetLevelWithSubclasses() {
        AlphabetNode node1 = new AlphabetNode("");
        AlphabetNode node2 = new AlphabetNode("");
        AlphabetLeaf leaf1 = new AlphabetLeaf(null, "");
        AlphabetLeaf leaf2 = new AlphabetLeaf(null, "");

        node1.addSubclass(node2);
        node1.addSubclass(leaf1);
        node2.addSubclass(leaf2);

        node1.resetLevel(2);

        assertEquals(2, node1.getLevel());
        assertEquals(3, node2.getLevel());
        assertEquals(3, leaf1.getLevel());
        assertEquals(4, leaf2.getLevel());
    }

    @Test
    public void testGetCharMapNoLeaves() {
        AlphabetNode node = new AlphabetNode("");
        Map<Character, Alphabet> expected = new HashMap<>();

        assertEquals(expected, node.getCharMap());
    }

    @Test
    public void testGetCharMapWithLeaves() {
        BitSet bitSet1 = new BitSet() {{
            set('a');
            set('b');
        }};
        BitSet bitSet2 = new BitSet() {{
            set('c');
            set('d');
        }};
        AlphabetNode node1 = new AlphabetNode("");
        AlphabetNode node2 = new AlphabetNode("");
        AlphabetLeaf leaf1 = new AlphabetLeaf(bitSet1, "");
        AlphabetLeaf leaf2 = new AlphabetLeaf(bitSet2, "");

        node1.addSubclass(node2);
        node1.addSubclass(leaf1);
        node2.addSubclass(leaf2);

        Map<Character, Alphabet> expected = new HashMap<>() {{
            put('a', leaf1);
            put('b', leaf1);
            put('c', leaf2);
            put('d', leaf2);
        }};

        assertEquals(expected, node1.getCharMap());
    }

    @Test
    public void testSetParent() {
        AlphabetNode node1 = new AlphabetNode("");
        AlphabetNode node2 = new AlphabetNode("");
        AlphabetLeaf leaf1 = new AlphabetLeaf(null, "");
        AlphabetLeaf leaf2 = new AlphabetLeaf(null, "");

        node1.addSubclass(node2);
        node1.addSubclass(leaf1);
        node2.addSubclass(leaf2);

        assertNull(node1.getParent());
        assertEquals(node1, node2.getParent());
        assertEquals(node1, leaf1.getParent());
        assertEquals(node2, leaf2.getParent());
    }
    
    @Test
    public void testGetDepth() {
        AlphabetNode node1 = new AlphabetNode("");
        AlphabetNode node2 = new AlphabetNode("");
        AlphabetLeaf leaf1 = new AlphabetLeaf(null, "");
        AlphabetLeaf leaf2 = new AlphabetLeaf(null, "");

        node1.addSubclass(node2);
        node1.addSubclass(leaf1);
        node2.addSubclass(leaf2);
        
        assertEquals(3, node1.getDepth());
        assertEquals(2, node2.getDepth());
    }

}
