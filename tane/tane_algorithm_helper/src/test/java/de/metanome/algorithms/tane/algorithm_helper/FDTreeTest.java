package de.metanome.algorithms.tane.algorithm_helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FDTreeTest {
    private FDTree fdtree;

    @Before
    public void setUp() throws Exception {
        fdtree = new FDTree(5);
        BitSet lhs = new BitSet();
        lhs.set(1);
        lhs.set(2);
        lhs.set(4);
        fdtree.addFunctionalDependency(lhs, 3);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testContainsSpecialization() {
        BitSet lhs = new BitSet();
        lhs.set(1);
        lhs.set(2);
        assertTrue(fdtree.containsSpecialization(lhs, 3, 0));
    }

    @Test
    public void testContainsGeneralization() {
        BitSet lhs = new BitSet();
        lhs.set(1);
        lhs.set(2);
        assertFalse(fdtree.containsGeneralization(lhs, 3, 0));
        lhs.set(4);
        lhs.set(5);
        assertTrue(fdtree.containsGeneralization(lhs, 3, 0));
    }

    @Test
    public void testGetSpecialization() {
        BitSet lhs = new BitSet();
        lhs.set(1);
        lhs.set(2);
        BitSet specLhs = new BitSet();
        assertTrue(fdtree.getSpecialization(lhs, 3, 0, specLhs));
        BitSet expResult = new BitSet();

        expResult.set(1);
        expResult.set(2);
        expResult.set(4);
        assertEquals(expResult, specLhs);

    }

    @Test
    public void testGetGeneralizationAndDelete() {
        BitSet lhs = new BitSet();
        lhs.set(1);
        lhs.set(2);
        lhs.set(4);
        lhs.set(5);
        BitSet specLhs = new BitSet();
        assertTrue(fdtree.getGeneralizationAndDelete(lhs, 3, 0, specLhs));

        BitSet expResult = new BitSet();

        expResult.set(1);
        expResult.set(2);
        expResult.set(4);
        assertEquals(expResult, specLhs);
    }

    @Test
    public void testFilterSpecialization() {
        BitSet lhs = new BitSet();
        lhs.set(1);
        lhs.set(4);
        fdtree.addFunctionalDependency(lhs, 3);

        fdtree.filterSpecializations();

        BitSet expResult = new BitSet();
        expResult.set(1);
        expResult.set(2);
        expResult.set(4);
        assertFalse(fdtree.containsGeneralization(lhs, 3, 0));
    }

    @Test
    public void testDeleteGeneralizations() {
        fdtree = new FDTree(4);
        BitSet lhs = new BitSet();
        lhs.set(1);
        lhs.set(2);

        fdtree.addFunctionalDependency(lhs, 4);
        lhs.clear(2);
        lhs.set(3);
        fdtree.addFunctionalDependency(lhs, 4);

        lhs.set(2);
        fdtree.deleteGeneralizations(lhs, 4, 0);
        assertTrue(fdtree.isEmpty());
    }

    @Test
    public void testContainsSpezialization() {
        FDTree fdtree = new FDTree(5);
        BitSet lhs = new BitSet();
        lhs.set(1);
        lhs.set(3);
        lhs.set(5);
        fdtree.addFunctionalDependency(lhs, 4);
        lhs.clear(1);
        lhs.set(2);
        fdtree.addFunctionalDependency(lhs, 4);

        lhs.clear(3);
        boolean result = fdtree.containsSpecialization(lhs, 4, 0);
        assertTrue(result);
    }
}
