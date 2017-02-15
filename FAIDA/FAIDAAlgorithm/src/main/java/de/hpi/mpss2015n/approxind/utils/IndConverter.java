package de.hpi.mpss2015n.approxind.utils;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.ColumnPermutation;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.results.InclusionDependency;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility to convert {@link SimpleInd}s to {@link InclusionDependency Metanome inclusion dependencies}.
 */
public final class IndConverter {

    private final List<List<ColumnIdentifier>> columnIdentifiersByTableAndColumn;

    public IndConverter(AbstractColumnStore[] columnStores) {
        this.columnIdentifiersByTableAndColumn = new ArrayList<>(columnStores.length);
        for (int i = 0; i < columnStores.length; i++) {
            AbstractColumnStore columnStore = columnStores[i];
            List<ColumnIdentifier> columnIdentifiers = new ArrayList<>(columnStore.getNumberOfColumns());
            for (String columnName : columnStore.getColumnNames()) {
                columnIdentifiers.add(new ColumnIdentifier(columnStore.getRelationName(), columnName));
            }
            this.columnIdentifiersByTableAndColumn.add(columnIdentifiers);
        }
    }

    public List<InclusionDependency> toMetanomeInds(List<SimpleInd> result) throws InputGenerationException, AlgorithmConfigurationException {
        ArrayList<InclusionDependency> converted = new ArrayList<>();
        for (SimpleInd simpleInd : result) {
            ColumnPermutation left = createPermutation(simpleInd.left);
            ColumnPermutation right = createPermutation(simpleInd.right);
            InclusionDependency inclusionDependency = new InclusionDependency(left, right);
            converted.add(inclusionDependency);
        }

        return converted;
    }


    private ColumnPermutation createPermutation(SimpleColumnCombination columnCombination) {
        ColumnIdentifier[] ids = new ColumnIdentifier[columnCombination.getColumns().length];
        List<ColumnIdentifier> identifier = this.columnIdentifiersByTableAndColumn.get(columnCombination.getTable());
        int j = 0;
        for (int i : columnCombination.getColumns()) {
            ids[j] = identifier.get(i); // ids[i] = identifier.get(i) throws out of bounce...
            j++;
        }
        return new ColumnPermutation(ids);
    }

}
