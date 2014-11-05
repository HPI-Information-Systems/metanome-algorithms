package de.metanome.algorithms.ducc;

import de.metanome.algorithm_helper.data_structures.ColumnCombinationBitset;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;
import de.metanome.algorithm_integration.results.UniqueColumnCombination;
import de.metanome.algorithms.ma2013n2.algorithm_helper.data_structures.GraphTraverser;
import de.metanome.algorithms.ma2013n2.algorithm_helper.data_structures.HoleFinder;
import de.metanome.algorithms.ma2013n2.algorithm_helper.data_structures.PruningGraph;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class UccGraphTraverser extends GraphTraverser {

  protected UniqueColumnCombinationResultReceiver resultReceiver;

  protected String relationName;
  protected List<String> columnNames;
  protected long desiredKeyError = 0;

  public UccGraphTraverser() {
    super();
  }

  public UccGraphTraverser(Random random) {
    this();
    this.random = random;
  }

  public void setDesiredKeyError(long desiredKeyError) {
    this.desiredKeyError = desiredKeyError;
  }

  public void init(List<PositionListIndex> basePLIs,
                   UniqueColumnCombinationResultReceiver resultReceiver, String relationName,
                   List<String> columnNames) throws CouldNotReceiveResultException {
    this.resultReceiver = resultReceiver;
    this.relationName = relationName;
    this.columnNames = columnNames;

    this.filterNonUniqueColumnCombinationBitsets(basePLIs);

    this.numberOfColumns = this.calculatedPlis.size();
    this.negativeGraph = new PruningGraph(this.numberOfColumns, this.OVERFLOW_THRESHOLD, false);
    this.positiveGraph = new PruningGraph(this.numberOfColumns, this.OVERFLOW_THRESHOLD, true);

    this.randomWalkTrace = new LinkedList<>();
    this.seedCandidates = this.buildInitialSeeds();

    this.holeFinder = new HoleFinder(this.numberOfColumns);
  }

  protected void filterNonUniqueColumnCombinationBitsets(List<PositionListIndex> basePLIs)
      throws CouldNotReceiveResultException {
    int columnIndex = 0;
    this.bitmaskForNonUniqueColumns = new ColumnCombinationBitset();

    for (PositionListIndex pli : basePLIs) {
      ColumnCombinationBitset currentColumnCombination = new ColumnCombinationBitset(columnIndex);
      this.calculatedPlis.put(currentColumnCombination, pli);
      if (isUnique(pli)) {
        this.minimalPositives.add(currentColumnCombination);
        this.resultReceiver.receiveResult(new UniqueColumnCombination(
            new ColumnIdentifier(this.relationName, columnNames.get(columnIndex))));
      } else {
        this.bitmaskForNonUniqueColumns.addColumn(columnIndex);
      }
      columnIndex++;
    }
  }

  protected List<ColumnCombinationBitset> buildInitialSeeds() {
    return this.bitmaskForNonUniqueColumns.getNSubsetColumnCombinations(2);
  }

  @Override
  protected boolean isPositiveColumnCombination(ColumnCombinationBitset currentColumnCombination) {
    return isUnique(this.getPLIFor(currentColumnCombination));
  }

  @Override
  protected void addMinimalPositive(ColumnCombinationBitset positiveColumnCombination)
      throws CouldNotReceiveResultException {
    this.minimalPositives.add(positiveColumnCombination);
    this.resultReceiver.receiveResult(new UniqueColumnCombination(
        positiveColumnCombination.createColumnCombination(this.relationName, this.columnNames)));
  }

  protected boolean isAdditionalConditionTrueForFindUnprunedSetAndUpdateGivenList(
      ColumnCombinationBitset singleSet) {
    return false;
  }

  protected boolean isUnique(PositionListIndex pli) {
    if (this.desiredKeyError == 0) {
      return pli.isUnique();
    } else {
      if (pli.getRawKeyError() <= this.desiredKeyError) {
        return true;
      } else {
        return false;
      }
    }
  }
}
