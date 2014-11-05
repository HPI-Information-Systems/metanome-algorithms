package de.metanome.algorithms.ducc;

import com.google.common.collect.ImmutableList;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;

import java.util.List;
import java.util.Map;
import java.util.Random;


public class DuccAlgorithm {

  public int found;
  protected String relationName;
  protected List<String> columnNames;
  protected UniqueColumnCombinationResultReceiver uccReceiver = null;
  protected UccGraphTraverser graphTraverser;
  protected long desiredRawKeyError = 0;

  public DuccAlgorithm(String relationName, List<String> columnNames,
                       UniqueColumnCombinationResultReceiver uccReceiver) {
    this.uccReceiver = uccReceiver;

    this.relationName = relationName;
    this.columnNames = columnNames;

    graphTraverser = new UccGraphTraverser();
  }

  public DuccAlgorithm(String relationName, List<String> columnNames,
                       UniqueColumnCombinationResultReceiver uccReceiver, Random random) {
    this(relationName, columnNames, uccReceiver);

    graphTraverser = new UccGraphTraverser(random);
  }

  /**
   * Method that sets the desired raw key error for unique checks. Default is 0. If set to a value
   * different to 0, partial uniques are found.
   */
  public void setRawKeyError(long keyError) {
    this.desiredRawKeyError = keyError;
    this.graphTraverser.setDesiredKeyError(keyError);
  }

  public void run(List<PositionListIndex> pliList) throws AlgorithmExecutionException {
    this.found = 0;
    this.graphTraverser.init(pliList, this.uccReceiver, this.relationName, this.columnNames);
    this.found = this.graphTraverser.traverseGraph();
  }

  public ImmutableList<ColumnCombinationBitset> getMinimalUniqueColumnCombinations() {
    return ImmutableList.copyOf(this.graphTraverser.getMinimalPositiveColumnCombinations());
  }

  public Map<ColumnCombinationBitset, PositionListIndex> getCalculatedPlis() {
    return this.graphTraverser.getCalculatedPlis();
  }

}
