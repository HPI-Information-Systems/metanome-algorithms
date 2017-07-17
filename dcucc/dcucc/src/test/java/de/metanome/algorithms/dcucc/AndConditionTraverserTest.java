package de.metanome.algorithms.dcucc;

import de.metanome.algorithm_helper.data_structures.PositionListIndex;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AndConditionTraverserTest {

  ConditionalPositionListIndexFixture fixture;

  @Before
  public void setup() {
    fixture = new ConditionalPositionListIndexFixture();
  }

  @Test
  public void testCalculateConditions() throws Exception {
    //Setup
    PositionListIndex uniquePLI = fixture.getUniquePLIForConditionTest();
    PositionListIndex conditionPLI = fixture.getConditionPLIForConditionTest();
    List<LongArrayList> expectedConditions = fixture.getExpectedConditions();
    //Execute functionality
    List<LongArrayList> unsatisfiedClusters = new LinkedList<>();
    AndConditionTraverser traverser = new AndConditionTraverser(new Dcucc());

    List<LongArrayList>
        actualConditions =
        traverser
            .calculateConditions(uniquePLI, conditionPLI, fixture.getFrequency(),
                                 unsatisfiedClusters);
    //Check result
    assertThat(actualConditions,
               IsIterableContainingInAnyOrder.containsInAnyOrder(
                   expectedConditions.toArray()
               )
    );
    assertEquals(unsatisfiedClusters.get(0), fixture.getExpectedUnsatisfiedClusters().get(0));
  }
}