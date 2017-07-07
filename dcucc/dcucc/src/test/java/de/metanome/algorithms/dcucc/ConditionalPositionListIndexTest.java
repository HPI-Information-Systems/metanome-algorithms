package de.metanome.algorithms.dcucc;

import org.junit.Before;

public class ConditionalPositionListIndexTest {

  ConditionalPositionListIndexFixture fixture;

  @Before
  public void setup() {
    fixture = new ConditionalPositionListIndexFixture();
  }

  /**
   * Test method for {@link de.metanome.algorithm_helper.data_structures.PositionListIndex::calculateConditions}
   */
 /* @Test
  public void testCalculateConditions() {
    //Setup
    PositionListIndex uniquePLI = fixture.getUniquePLIForConditionTest();
    PositionListIndex conditionPLI = fixture.getConditionPLIForConditionTest();
    List<LongArrayList> expectedConditions = fixture.getExpectedConditions();
    //Execute functionality
    List<LongArrayList> unsatisfiedClusters = new LinkedList<>();

    List<LongArrayList>
        actualConditions =
        ConditionalPositionListIndex
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

  @Test
  public void testCalculateConditionsNot() {
    //Setup
    PositionListIndex uniquePLI = fixture.getUniquePLIForNotConditionTest();
    PositionListIndex conditionPLI = fixture.getConditionPLIForNotConditionTest();
    List<LongArrayList> expectedConditions = fixture.getExpectedNotConditions();

    //Execute functionality
    List<LongArrayList>
        actualConditions =
        ConditionalPositionListIndex
            .calculateNotConditions(uniquePLI, conditionPLI, fixture.getFrequency(),
                                    fixture.getNumberOfTuplesEmptyTest());
    //Check result
    assertThat(actualConditions,
               IsIterableContainingInAnyOrder.containsInAnyOrder(
                   expectedConditions.toArray()
               )
    );
  }

  @Test
  public void testCalculateConditionsNotEmpty() {
    //Setup
    PositionListIndex uniquePLI = fixture.getUniquePLIForNotConditionEmptyTest();
    PositionListIndex conditionPLI = fixture.getConditionPLIForNotConditionEmptyTest();

    //Execute functionality
    List<LongArrayList>
        actualConditions =
        ConditionalPositionListIndex
            .calculateNotConditions(uniquePLI, conditionPLI, fixture.getFrequency(),
                                    fixture.getNumberOfTuplesEmptyTest());
    //Check result
    assertTrue(actualConditions.isEmpty());
  }*/
}