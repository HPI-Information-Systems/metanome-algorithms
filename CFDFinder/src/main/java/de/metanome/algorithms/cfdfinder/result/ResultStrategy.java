package de.metanome.algorithms.cfdfinder.result;

import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.ConditionalFunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.results.ConditionalFunctionalDependency;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class ResultStrategy {

    private ConditionalFunctionalDependencyResultReceiver resultReceiver;
    private ObjectArrayList<ColumnIdentifier> columnIdentifiers;
    protected int numResults = 0;

    public ResultStrategy(ConditionalFunctionalDependencyResultReceiver resultReceiver, ObjectArrayList<ColumnIdentifier> columnIdentifiers) {
        this.resultReceiver = resultReceiver;
        this.columnIdentifiers = columnIdentifiers;
    }

    public void startReceiving() {}
    public abstract void receiveResult(Result result);
    public void stopReceiving() {}

    protected void sendToMetanome(Result result) {
        this.numResults += 1;

        ColumnIdentifier[] lhsColumns = new ColumnIdentifier[result.getEmbeddedFD().lhs.cardinality()];
        int j = 0;
        for (int i = result.getEmbeddedFD().lhs.nextSetBit(0); i >= 0; i = result.getEmbeddedFD().lhs.nextSetBit(i + 1)) {
            int columnId = i; // todo: Here we translate the column i back to the real column i before the sorting
            lhsColumns[j++] = columnIdentifiers.get(columnId);
        }

        ColumnCombination colCombination = new ColumnCombination(lhsColumns);
        int rhsId = result.getEmbeddedFD().rhs; // todo: Here we translate the column rhs back to the real column rhs before the sorting
        ConditionalFunctionalDependency cfdResult = new ConditionalFunctionalDependency(
                colCombination, columnIdentifiers.get(rhsId), result.getPatternTableauAsString());
        try {
            resultReceiver.receiveResult(cfdResult);
        } catch (CouldNotReceiveResultException | ColumnNameMismatchException e) {
            e.printStackTrace();
        }
    }

    public int getNumResults() {
        return numResults;
    }
}
