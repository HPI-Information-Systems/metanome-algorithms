package de.metanome.algorithms.dcucc;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_integration.AlgorithmExecutionException;

/**
 * @author Jens Ehrlich
 */
public interface ConditionLatticeTraverser {

  public void iterateConditionLattice(ColumnCombinationBitset partialUnique)
      throws AlgorithmExecutionException;


}
