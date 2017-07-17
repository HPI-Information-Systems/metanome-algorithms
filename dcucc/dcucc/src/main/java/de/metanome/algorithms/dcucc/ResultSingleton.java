package de.metanome.algorithms.dcucc;

import com.google.common.collect.ImmutableList;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.SubSetGraph;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnCondition;
import de.metanome.algorithm_integration.ColumnConditionAnd;
import de.metanome.algorithm_integration.ColumnConditionOr;
import de.metanome.algorithm_integration.ColumnConditionValue;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.ConditionalUniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Jens Ehrlich
 */
public class ResultSingleton {

  protected static ResultSingleton singleton;

  protected SubSetGraph conditionMinimalityGraph;
  protected List<Map<Long, String>> inputMap;
  protected Set<Condition> foundConditions;
  protected RelationalInput input;
  private ConditionalUniqueColumnCombinationResultReceiver resultReceiver;

  protected ResultSingleton(RelationalInput input,
                            ImmutableList<ColumnCombinationBitset> partialUccs,
                            ConditionalUniqueColumnCombinationResultReceiver receiver)
      throws InputGenerationException, InputIterationException {
    prepareOutput(input);
    this.resultReceiver = receiver;
    this.input = input;
    this.conditionMinimalityGraph = new SubSetGraph();
    this.foundConditions = new HashSet<>();
    this.conditionMinimalityGraph.addAll(partialUccs);

  }

  public static ResultSingleton getInstance() {
    return singleton;
  }

  public static ResultSingleton createResultSingleton(RelationalInput input,
                                                      ImmutableList<ColumnCombinationBitset> partialUccs,
                                                      ConditionalUniqueColumnCombinationResultReceiver receiver)
      throws InputGenerationException, InputIterationException {
    singleton = new ResultSingleton(input, partialUccs, receiver);
    return singleton;
  }

  protected void prepareOutput(RelationalInput input)
      throws InputGenerationException, InputIterationException {
    this.inputMap = new ArrayList<>(input.numberOfColumns());
    for (int i = 0; i < input.numberOfColumns(); i++) {
      inputMap.add(new HashMap<Long, String>());
    }
    long row = 0;
    while (input.hasNext()) {
      List<String> values = input.next();
      for (int i = 0; i < input.numberOfColumns(); i++) {
        inputMap.get(i).put(row, values.get(i));
      }
      row++;
    }
  }

  protected void addMinimalConditionToResult(ColumnCombinationBitset partialUnique,
                                             List<ConditionEntry> singleCondition)
      throws AlgorithmExecutionException {
    Map<ColumnCombinationBitset, SingleCondition> conditionMap = new HashMap<>();
    for (ConditionEntry entry : singleCondition) {
      if (conditionMap.containsKey(entry.condition)) {
        conditionMap.get(entry.condition).addCluster(entry.cluster.get(0), entry.coverage);
      } else {
        SingleCondition resultCondition = new SingleCondition();
        resultCondition.addCluster(entry.cluster.get(0), entry.coverage);
        conditionMap.put(entry.condition, resultCondition);
      }
    }
    ResultSingleton result = ResultSingleton.getInstance();
    Condition resultCondition = new Condition(partialUnique, conditionMap);
    //result.receiveResult(resultCondition);
    this.addOrResultToResultReceiver(resultCondition);
  }

  protected void addConditionToResult(ColumnCombinationBitset partialUnique,
                                      List<ConditionEntry> singleCondition)
      throws AlgorithmExecutionException {
    List<Condition.ConditionElement> conditionMap = new ArrayList<>();

    Condition resultCondition = new Condition(partialUnique, singleCondition);

    ResultSingleton result = ResultSingleton.getInstance();
    result.receiveResult(resultCondition);
  }

  protected void addConditionToResult(ColumnCombinationBitset partialUnique,
                                      List<ConditionEntry> singleCondition, float coverage)
      throws AlgorithmExecutionException {
    List<Condition.ConditionElement> conditionMap = new ArrayList<>();

    Condition resultCondition = new Condition(partialUnique, singleCondition);
    resultCondition.coverage = coverage;

    ResultSingleton result = ResultSingleton.getInstance();
    result.receiveResult(resultCondition);
  }


  protected boolean checkConditionMinimality(ColumnCombinationBitset partialUnique,
                                             Condition condition) {
    for (ColumnCombinationBitset subset : this.conditionMinimalityGraph
        .getExistingSubsets(partialUnique)) {
      condition.partialUnique = subset;
      if (this.foundConditions.contains(condition)) {
        return true;
      }
    }
    return false;
  }

  public void receiveResult(ConditionalUniqueColumnCombination result)
          throws CouldNotReceiveResultException, ColumnNameMismatchException {
    this.resultReceiver.receiveResult(result);
  }

  public void receiveResult(Condition resultCondition) throws AlgorithmExecutionException {
    if (this.checkConditionMinimality(resultCondition.partialUnique, resultCondition)) {
      return;
    }
    this.foundConditions.add(resultCondition);
    this.addOrResultToResultReceiver(resultCondition);
  }

  public void addOrResultToResultReceiver(Condition condition)
      throws AlgorithmExecutionException {

    ColumnConditionOr columnCondition = new ColumnConditionOr();
    if (!Float.isNaN(condition.coverage)) {
      columnCondition.setCoverage(condition.coverage);
    }
    //build condition
    for (Condition.ConditionElement conditionElement : condition.conditions) {
      ColumnCombinationBitset conditionColumn = conditionElement.condition;
      if (conditionColumn.size() == 1) {
        addValuesToCondition(columnCondition, conditionColumn,
                             conditionElement.value);
      } else {
        ColumnConditionAnd andCondition = new ColumnConditionAnd();
        for (ColumnCombinationBitset singleBitset : conditionColumn
            .getContainedOneColumnCombinations()) {
          addValuesToCondition(andCondition, singleBitset,
                               conditionElement.value);
        }
        columnCondition.add(andCondition);
      }
    }

    ConditionalUniqueColumnCombination
        conditionalUniqueColumnCombination =
        new ConditionalUniqueColumnCombination(
            condition.partialUnique
                .createColumnCombination(input.relationName(), input.columnNames()),
            columnCondition);

    ResultSingleton.getInstance().receiveResult(conditionalUniqueColumnCombination);
  }

  protected void addValuesToCondition(ColumnCondition columnCondition,
                                      ColumnCombinationBitset conditionColumn,
                                      SingleCondition singleCondition) {

    Map<String, Float> conditionValues = new TreeMap<>();
    for (long index : singleCondition.getCluster()) {
      conditionValues.put(inputMap.get(conditionColumn.getSetBits().get(0)).get(index),
                          singleCondition.cluster.get(index));
    }
    for (String conditionValue : conditionValues.keySet()) {
      ColumnIdentifier
          identifier =
          new ColumnIdentifier(input.relationName(),
                               input.columnNames().get(conditionColumn.getSetBits().get(0)));
      ColumnConditionValue
          leaf =
          new ColumnConditionValue(identifier, conditionValue, singleCondition.isNegated);
      leaf.setCoverage(conditionValues.get(conditionValue));
      columnCondition.add(leaf);
    }
  }

}
