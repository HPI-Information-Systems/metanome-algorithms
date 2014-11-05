package de.metanome.algorithms.ma2013n2.algorithm_helper.data_structures;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


public class PruningGraphFixture {

  protected ColumnCombinationBitset columnCombinationA = new ColumnCombinationBitset(0);
  protected ColumnCombinationBitset columnCombinationB = new ColumnCombinationBitset(1);
  protected ColumnCombinationBitset columnCombinationC = new ColumnCombinationBitset(2);
  protected ColumnCombinationBitset columnCombinationD = new ColumnCombinationBitset(3);


  protected ColumnCombinationBitset columnCombinationAB;
  protected ColumnCombinationBitset columnCombinationAC;
  protected ColumnCombinationBitset columnCombinationBC;
  protected ColumnCombinationBitset columnCombinationBD;
  protected ColumnCombinationBitset columnCombinationABC;

  public PruningGraphFixture() {
    // AB
    columnCombinationAB = new ColumnCombinationBitset(0, 1);

    // AC Column
    columnCombinationAC = new ColumnCombinationBitset(0, 2);

    // BC Column
    columnCombinationBC = new ColumnCombinationBitset(1, 2);

    //BD Column
    columnCombinationBD = new ColumnCombinationBitset(1, 3);

    // ABC Column
    columnCombinationABC = new ColumnCombinationBitset(0, 1, 2);
  }

  protected List<ColumnCombinationBitset> getListOf(ColumnCombinationBitset... columnCombinations) {
    List<ColumnCombinationBitset> columnCombinationList = new LinkedList<>();

    Collections.addAll(columnCombinationList, columnCombinations);

    return columnCombinationList;
  }

  public PruningGraph getGraphWith1Element() {
    PruningGraph actualGraph = new PruningGraph(5, 10, true);

    actualGraph.columnCombinationMap.put(columnCombinationB, getListOf(columnCombinationBC));
    actualGraph.columnCombinationMap.put(columnCombinationC, getListOf(columnCombinationBC));

    return actualGraph;
  }

  public PruningGraph getGraphWith2ElementAndOverflow() {
    PruningGraph actualGraph = new PruningGraph(5, 2, true);

    actualGraph.columnCombinationMap.put(columnCombinationB, actualGraph.OVERFLOW);
    actualGraph.columnCombinationMap.put(columnCombinationC, getListOf(columnCombinationBC));
    actualGraph.columnCombinationMap.put(columnCombinationBC, getListOf(columnCombinationBC));
    actualGraph.columnCombinationMap.put(columnCombinationBD, getListOf(columnCombinationBD));
    actualGraph.columnCombinationMap.put(columnCombinationD, getListOf(columnCombinationBD));

    return actualGraph;
  }

  public PruningGraph getGraphWith2ElementAndOverflowNonUnique() {
    PruningGraph actualGraph = new PruningGraph(3, 2, false);

    actualGraph.columnCombinationMap.put(columnCombinationB, actualGraph.OVERFLOW);
    actualGraph.columnCombinationMap.put(columnCombinationC, getListOf(columnCombinationBC));
    actualGraph.columnCombinationMap.put(columnCombinationBC, getListOf(columnCombinationBC));
    actualGraph.columnCombinationMap.put(columnCombinationBD, getListOf(columnCombinationBD));
    actualGraph.columnCombinationMap.put(columnCombinationD, getListOf(columnCombinationBD));

    return actualGraph;
  }

  // FIXME this method is nerver called
  public PruningGraph getGraphForMinimalUniques() {
    PruningGraph actualGraph = new PruningGraph(5, 10000, true);
    actualGraph.columnCombinationMap
        .put(columnCombinationA, getListOf(columnCombinationAB, columnCombinationAC));
    actualGraph.columnCombinationMap.put(columnCombinationB, getListOf(columnCombinationAB));
    actualGraph.columnCombinationMap.put(columnCombinationC, getListOf(columnCombinationAC));
    actualGraph.columnCombinationMap.put(columnCombinationD, getListOf(columnCombinationD));
    return actualGraph;
  }

  // FIXME this method is nerver called
  public Collection<ColumnCombinationBitset> getExpectedMinimalUniques() {
    Collection<ColumnCombinationBitset> expectedUniques = new HashSet<>();
    expectedUniques.add(new ColumnCombinationBitset(0, 1));
    expectedUniques.add(new ColumnCombinationBitset(0, 2));
    expectedUniques.add(new ColumnCombinationBitset(3));
    return expectedUniques;
  }
}
