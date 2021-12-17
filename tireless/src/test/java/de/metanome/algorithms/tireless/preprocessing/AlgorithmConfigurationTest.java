package de.metanome.algorithms.tireless.preprocessing;

import org.junit.Test;

import static org.junit.Assert.*;

public class AlgorithmConfigurationTest {

    int a = 1;
    double b = 2.5;
    int c = 3;
    double d = 4.5;
    int e = 5;
    int f = 6;
    double g = 7.5;

    @Test
    public void testConfiguration() {
        AlgorithmConfiguration configuration = new AlgorithmConfiguration(a, b, c, d, e, f, g);

        assertEquals(a, configuration.MAXIMUM_ELEMENT_COUNT);
        assertEquals(b, configuration.MINIMAL_SPECIAL_CHARACTER_OCCURRENCE, 0.1);
        assertEquals(c, configuration.DISJUNCTION_MERGING_THRESHOLD);
        assertEquals(d, configuration.MAXIMUM_LENGTH_DEVIATION_FACTOR, 0.1);
        assertEquals(e, configuration.CHAR_CLASS_GENERALIZATION_THRESHOLD);
        assertEquals(f, configuration.QUANTIFIER_GENERALIZATION_THRESHOLD);
        assertEquals(g, configuration.OUTLIER_THRESHOLD, 0.1);
    }

    @Test
    public void testRangeIntervals() {
        AlgorithmConfiguration configuration = new AlgorithmConfiguration(a, b, c, d, e, f, g);

        assertTrue(configuration.charIsInRange('a'));
        assertTrue(configuration.charIsInRange('z'));
        assertTrue(configuration.charIsInRange('A'));
        assertTrue(configuration.charIsInRange('Z'));
        assertTrue(configuration.charIsInRange('0'));
        assertTrue(configuration.charIsInRange('1'));
        assertTrue(configuration.charIsInRange('9'));

        assertFalse(configuration.charIsInRange('/'));
        assertFalse(configuration.charIsInRange(':'));
        assertFalse(configuration.charIsInRange('@'));
        assertFalse(configuration.charIsInRange('['));
        assertFalse(configuration.charIsInRange('`'));
        assertFalse(configuration.charIsInRange('{'));
    }
}
