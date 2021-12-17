package de.metanome.algorithms.tireless.preprocessing.alphabet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AlphabetTest {

    @Test
    public void testSuperClassOfLevelHigherLevel() {
        AlphabetNode node1 = new AlphabetNode("");
        AlphabetNode node2 = new AlphabetNode("");
        node1.addSubclass(node2);
        node1.resetLevel(0);

        Alphabet result = node2.getSuperclassOfLevel(0);

        assertEquals(node1, result);
    }

    @Test
    public void testSuperClassOfLevelSameLevel() {
        AlphabetNode node1 = new AlphabetNode("");
        AlphabetNode node2 = new AlphabetNode("");
        node1.addSubclass(node2);
        node1.resetLevel(0);

        Alphabet result = node2.getSuperclassOfLevel(1);

        assertEquals(node2, result);
    }

    @Test
    public void testSuperClassOfLevelNoParent() {
        AlphabetNode node1 = new AlphabetNode("");
        AlphabetNode node2 = new AlphabetNode("");
        node1.addSubclass(node2);
        node1.resetLevel(0);

        Alphabet result = node2.getSuperclassOfLevel(-1);

        assertEquals(node1, result);
    }
}
