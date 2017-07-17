package de.metanome.algorithms.dcucc;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.ColumnCondition;
import de.metanome.algorithm_integration.ColumnConditionAnd;
import de.metanome.algorithm_integration.ColumnConditionValue;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.results.ConditionalUniqueColumnCombination;

import it.unimi.dsi.fastutil.longs.LongArrayList;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Jens Ehrlich
 */
public class SelfConditionFinder {

  public static void calculateSelfConditions(ColumnCombinationBitset partialUnique,
                                             PositionListIndex partialUniquePLI, Dcucc algorithm)
          throws CouldNotReceiveResultException, ColumnNameMismatchException {

    if ((partialUniquePLI.getRawKeyError() + partialUniquePLI.getClusters().size())
        >= algorithm.frequency) {
      return;
    }
    ResultSingleton singleton = ResultSingleton.getInstance();
    ColumnCondition outerCondition = new ColumnConditionAnd();
    for (LongArrayList cluster : partialUniquePLI.getClusters()) {

      ColumnConditionAnd innerCondition = new ColumnConditionAnd();
      innerCondition.setNegated(true);
      outerCondition.add(innerCondition);
      for (ColumnCombinationBitset singleColumn : partialUnique
          .getContainedOneColumnCombinations()) {
        Set<String> values = new HashSet<>();
        for (long row : cluster) {
          values.add(singleton.inputMap.get(singleColumn.getSetBits().get(0)).get(row));
        }

        for (String value : values) {
          ColumnCondition
              conditionValue =
              new ColumnConditionValue(new ColumnIdentifier(singleton.input.relationName(),
                                                            singleton.input.columnNames().get(
                                                                singleColumn.getSetBits().get(0))),
                                       value);
          innerCondition.add(conditionValue);
        }
      }
    }

    ConditionalUniqueColumnCombination
        result =
        new ConditionalUniqueColumnCombination(partialUnique.createColumnCombination(
            singleton.input.relationName(), singleton.input.columnNames()), outerCondition);
    singleton.receiveResult(result);
  }
}
