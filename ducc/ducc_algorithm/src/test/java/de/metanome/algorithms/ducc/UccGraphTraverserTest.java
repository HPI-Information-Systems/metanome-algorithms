package de.metanome.algorithms.ducc;

import com.google.common.collect.ImmutableList;
import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class UccGraphTraverserTest {

    protected UccGraphTraverserFixture fixture;

    @Before
    public void setUp() {
        fixture = new UccGraphTraverserFixture();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testTraverseGraph() throws CouldNotReceiveResultException {
        // Setup
        List<PositionListIndex> pliList = fixture.getPLIList();
        LinkedList<String> columnNames = new LinkedList<>();
        Integer i;
        for (i = 0; i < pliList.size(); i++) {
            columnNames.add(i.toString());
        }
        ImmutableList<String> immutableColumnNames = ImmutableList.copyOf(columnNames);

        UccGraphTraverser graph = new UccGraphTraverser();
        graph.init(fixture.getPLIList(), mock(UniqueColumnCombinationResultReceiver.class), "relation", immutableColumnNames);
        Collection<ColumnCombinationBitset> expectedUniqueColumnCombinations = fixture.getExpectedBitset();

        //Execute functionality
        graph.traverseGraph();

        //Check result
        assertThat(graph.getMinimalPositiveColumnCombinations(), IsIterableContainingInAnyOrder.containsInAnyOrder(expectedUniqueColumnCombinations.toArray(new ColumnCombinationBitset[expectedUniqueColumnCombinations.size()])));
    }

}
