package de.metanome.algorithms.cfdfinder.pattern;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class PatternTest {

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testPatternEntries() {
        VariablePatternEntry variablePatternEntry = new VariablePatternEntry();
        ConstantPatternEntry constantPatternEntry = new ConstantPatternEntry(42);

        Assert.assertEquals(variablePatternEntry, new VariablePatternEntry());
        Assert.assertNotEquals(variablePatternEntry, constantPatternEntry);
    }

    @Test
    public void testVariablePatternEntryMatching() {
        VariablePatternEntry entry = new VariablePatternEntry();
        Assert.assertTrue(entry.matches(1));
        Assert.assertTrue(entry.matches(123));
        Assert.assertTrue(entry.matches(444));
    }

    @Test
    public void testConstantPatternEntryMatching() {
        ConstantPatternEntry entry = new ConstantPatternEntry(42);
        Assert.assertFalse(entry.matches(1));
        Assert.assertFalse(entry.matches(123));
        Assert.assertFalse(entry.matches(444));
        Assert.assertTrue(entry.matches(42));
    }

    @Test
    public void testPatternMatching() {
        Map<Integer, PatternEntry> attributes = new HashMap<>();
        attributes.put(0, new VariablePatternEntry());
        attributes.put(1, new ConstantPatternEntry(2));
        attributes.put(2, new ConstantPatternEntry(4));
        Pattern pattern = new Pattern(attributes);

        Assert.assertTrue(pattern.matches(new int[] {1, 2, 4}));
        Assert.assertTrue(pattern.matches(new int[] {6, 2, 4}));
        Assert.assertFalse(pattern.matches(new int[] {6, 2, 5}));
        Assert.assertFalse(pattern.matches(new int[] {6, 3, 4}));
    }

    @Test
    public void testPatternEquality() {
        Map<Integer, PatternEntry> attributes = new HashMap<>();
        attributes.put(0, new VariablePatternEntry());
        attributes.put(1, new ConstantPatternEntry(2));
        attributes.put(2, new ConstantPatternEntry(4));
        Pattern pattern = new Pattern(attributes);

        //noinspection ObjectEqualsNull
        Assert.assertNotEquals(pattern, null);

        Map<Integer, PatternEntry> attributes2 = new HashMap<>();
        attributes2.put(0, new VariablePatternEntry());
        attributes2.put(1, new ConstantPatternEntry(2));
        attributes2.put(2, new ConstantPatternEntry(4));
        Pattern pattern2 = new Pattern(attributes2);

        Assert.assertEquals(pattern, pattern2);

        Map<Integer, PatternEntry> attributes3 = new HashMap<>();
        attributes3.put(0, new VariablePatternEntry());
        attributes3.put(1, new ConstantPatternEntry(3));
        attributes3.put(2, new ConstantPatternEntry(5));
        Pattern pattern3 = new Pattern(attributes3);

        Assert.assertNotEquals(pattern, pattern3);
    }
}

