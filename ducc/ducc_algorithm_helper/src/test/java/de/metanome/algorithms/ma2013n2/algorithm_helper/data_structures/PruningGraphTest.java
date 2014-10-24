package de.metanome.algorithms.ma2013n2.algorithm_helper.data_structures;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class PruningGraphTest {
    PruningGraphFixture fixture = new PruningGraphFixture();

    /**
     * Unique Graph tests
     */
    @Test
    public void testAddSimpleInsert() {
        //Setup
        PruningGraph graph = new PruningGraph(5, 10, true);

        ColumnCombinationBitset actualColumnCombinationAC = fixture.columnCombinationAC;
        ColumnCombinationBitset actualColumnCombinationBC = fixture.columnCombinationBC;

        ColumnCombinationBitset expectedKeyA = fixture.columnCombinationA;
        ColumnCombinationBitset expectedKeyB = fixture.columnCombinationB;
        ColumnCombinationBitset expectedKeyC = fixture.columnCombinationC;

        List<ColumnCombinationBitset> expectedList = new LinkedList<>();
        expectedList.add(actualColumnCombinationAC);
        expectedList.add(actualColumnCombinationBC);

        //Execute functionality
        graph.add(actualColumnCombinationAC);
        graph.add(actualColumnCombinationBC);

        //Check results
        assertTrue(graph.columnCombinationMap.containsKey(expectedKeyA));
        assertEquals(actualColumnCombinationAC, graph.columnCombinationMap.get(expectedKeyA).get(0));

        assertTrue(graph.columnCombinationMap.containsKey(expectedKeyB));
        assertEquals(actualColumnCombinationBC, graph.columnCombinationMap.get(expectedKeyB).get(0));

        assertTrue(graph.columnCombinationMap.containsKey(expectedKeyC));
        assertThat(graph.columnCombinationMap.get(expectedKeyC), IsIterableContainingInAnyOrder.containsInAnyOrder(expectedList.toArray(new ColumnCombinationBitset[expectedList.size()])));

    }

    @Test
    public void testAddPruneGraph() {
        //Setup
        PruningGraph graph = new PruningGraph(5, 10, true);

        ColumnCombinationBitset actualColumnCombinationAC = fixture.columnCombinationAC;
        ColumnCombinationBitset actualColumnCombinationABC = fixture.columnCombinationABC;

        ColumnCombinationBitset expectedKeyA = fixture.columnCombinationA;
        ColumnCombinationBitset expectedKeyB = fixture.columnCombinationB;
        ColumnCombinationBitset expectedKeyC = fixture.columnCombinationC;

        //Execute functionality
        graph.add(actualColumnCombinationAC);
        graph.add(actualColumnCombinationABC);

        //Check results
        assertTrue(graph.columnCombinationMap.containsKey(expectedKeyA));
        assertTrue(graph.columnCombinationMap.containsKey(expectedKeyC));
        assertTrue(graph.columnCombinationMap.containsKey(expectedKeyB));

        assertEquals(actualColumnCombinationAC, graph.columnCombinationMap.get(expectedKeyA).get(0));
        assertEquals(actualColumnCombinationABC, graph.columnCombinationMap.get(expectedKeyB).get(0));
        assertEquals(actualColumnCombinationAC, graph.columnCombinationMap.get(expectedKeyC).get(0));

        assertEquals(1, graph.columnCombinationMap.get(expectedKeyA).size());
        assertEquals(1, graph.columnCombinationMap.get(expectedKeyB).size());
        assertEquals(1, graph.columnCombinationMap.get(expectedKeyC).size());
    }

    @Test
    public void testAddPruneGraphRemoveEntry() {
        //Setup
        PruningGraph graph = new PruningGraph(5, 10, true);

        ColumnCombinationBitset actualColumnCombinationAC = fixture.columnCombinationAC;
        ColumnCombinationBitset actualColumnCombinationABC = fixture.columnCombinationABC;

        ColumnCombinationBitset expectedKeyA = fixture.columnCombinationA;
        ColumnCombinationBitset expectedKeyB = fixture.columnCombinationB;
        ColumnCombinationBitset expectedKeyC = fixture.columnCombinationC;

        //Execute functionality
        graph.add(actualColumnCombinationABC);
        graph.add(actualColumnCombinationAC);

        //Check result
        assertTrue(graph.columnCombinationMap.containsKey(expectedKeyA));
        assertTrue(graph.columnCombinationMap.containsKey(expectedKeyB));
        assertTrue(graph.columnCombinationMap.containsKey(expectedKeyC));

        assertEquals(1, graph.columnCombinationMap.get(expectedKeyA).size());
        assertEquals(1, graph.columnCombinationMap.get(expectedKeyB).size());
        assertEquals(1, graph.columnCombinationMap.get(expectedKeyC).size());

        assertEquals(actualColumnCombinationAC, graph.columnCombinationMap.get(expectedKeyA).get(0));
        assertEquals(actualColumnCombinationAC, graph.columnCombinationMap.get(expectedKeyC).get(0));

        assertEquals(actualColumnCombinationABC, graph.columnCombinationMap.get(expectedKeyB).get(0));
    }

    @Test
    public void testAddReplicatedAdd() {
        //Setup
        PruningGraph graph = new PruningGraph(5, 10, true);

        ColumnCombinationBitset actualColumnCombinationAC = fixture.columnCombinationAC;

        ColumnCombinationBitset expectedKeyA = fixture.columnCombinationA;
        ColumnCombinationBitset expectedKeyC = fixture.columnCombinationC;

        //Execute functionality
        graph.add(actualColumnCombinationAC);
        graph.add(actualColumnCombinationAC);

        //Check results
        assertEquals(1, graph.columnCombinationMap.get(expectedKeyA).size());
        assertEquals(1, graph.columnCombinationMap.get(expectedKeyC).size());
    }

    @Test
    public void addTestOverflow() {
        //Setup
        PruningGraph graph = new PruningGraph(5, 2, true);

        ColumnCombinationBitset actualColumnCombinationAB = fixture.columnCombinationAB;
        ColumnCombinationBitset actualColumnCombinationBC = fixture.columnCombinationBC;
        ColumnCombinationBitset actualColumnCombinationBD = fixture.columnCombinationBD;

        ColumnCombinationBitset expectedKeyB = fixture.columnCombinationB;

        //Execute functionality
        graph.add(actualColumnCombinationAB);
        graph.add(actualColumnCombinationBC);
        graph.add(actualColumnCombinationBD);

        //Check results
        assertEquals(graph.OVERFLOW, graph.columnCombinationMap.get(expectedKeyB));

        assertEquals(1, graph.columnCombinationMap.get(actualColumnCombinationAB).size());
        assertEquals(actualColumnCombinationAB, graph.columnCombinationMap.get(actualColumnCombinationAB).get(0));

        assertEquals(1, graph.columnCombinationMap.get(actualColumnCombinationBC).size());
        assertEquals(actualColumnCombinationBC, graph.columnCombinationMap.get(actualColumnCombinationBC).get(0));

        assertEquals(1, graph.columnCombinationMap.get(actualColumnCombinationBD).size());
        assertEquals(actualColumnCombinationBD, graph.columnCombinationMap.get(actualColumnCombinationBD).get(0));
    }

    /**
     * Test method for {@link PruningGraph#find(ColumnCombinationBitset)}
     * <p/>
     * TODO docs
     */
    @Test
    public void testFind() {
        // Setup
        PruningGraph graph = fixture.getGraphWith1Element();

        // Execute functionality
        // Check result
        assertFalse(graph.find(fixture.columnCombinationAC));
        assertTrue(graph.find(fixture.columnCombinationABC));
    }

    @Test
    public void testFindWithOverflow() {
        // Setup
        PruningGraph graph = fixture.getGraphWith2ElementAndOverflow();

        // Execute functionality
        // Check result
        assertFalse(graph.find(fixture.columnCombinationAC));
        assertTrue(graph.find(fixture.columnCombinationABC));
    }


    /**
     * Non Unique Graph tests
     */

    @Test
    public void testAddPruneGraphNonUnique() {
        //Setup
        PruningGraph graph = new PruningGraph(5, 10, false);

        ColumnCombinationBitset actualColumnCombinationAC = fixture.columnCombinationAC;
        ColumnCombinationBitset actualColumnCombinationABC = fixture.columnCombinationABC;

        ColumnCombinationBitset expectedKeyA = fixture.columnCombinationA;
        ColumnCombinationBitset expectedKeyB = fixture.columnCombinationB;
        ColumnCombinationBitset expectedKeyC = fixture.columnCombinationC;

        //Execute functionality
        graph.add(actualColumnCombinationAC);
        graph.add(actualColumnCombinationABC);


        //Check result
        assertTrue(graph.columnCombinationMap.containsKey(expectedKeyA));
        assertTrue(graph.columnCombinationMap.containsKey(expectedKeyC));
        assertTrue(graph.columnCombinationMap.containsKey(expectedKeyB));

        assertEquals(actualColumnCombinationABC, graph.columnCombinationMap.get(expectedKeyA).get(0));
        assertEquals(actualColumnCombinationABC, graph.columnCombinationMap.get(expectedKeyB).get(0));
        assertEquals(actualColumnCombinationABC, graph.columnCombinationMap.get(expectedKeyC).get(0));

        assertEquals(1, graph.columnCombinationMap.get(expectedKeyA).size());
        assertEquals(1, graph.columnCombinationMap.get(expectedKeyB).size());
        assertEquals(1, graph.columnCombinationMap.get(expectedKeyC).size());
    }

    @Test
    public void testFindWithOverflowNonUnique() {
        // Setup
        PruningGraph graph = fixture.getGraphWith2ElementAndOverflowNonUnique();

        // Execute functionality
        // Check result
        assertFalse(graph.find(fixture.columnCombinationAC));
        assertTrue(graph.find(fixture.columnCombinationB));
        assertTrue(graph.find(fixture.columnCombinationBC));
    }
}
