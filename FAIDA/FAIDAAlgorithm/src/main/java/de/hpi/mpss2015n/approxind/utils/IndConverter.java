package de.hpi.mpss2015n.approxind.utils;

import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.ColumnPermutation;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.results.InclusionDependency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IndConverter {

    private final RelationalInputGenerator[] fileInputGenerators;
    private final Logger logger = LoggerFactory.getLogger(getClass());


    public IndConverter(RelationalInputGenerator[] fileInputGenerators) {
        this.fileInputGenerators = fileInputGenerators;
    }


    public List<InclusionDependency> toMetanomeInds(List<SimpleInd> result, String[] tablenames) throws InputGenerationException {
        List<List<ColumnIdentifier>> columns = new ArrayList<>();
        for (int i = 0; i < fileInputGenerators.length; i++) {
            RelationalInput input = fileInputGenerators[i].generateNewCopy();
            List<ColumnIdentifier> identifiers = new ArrayList<>();
            for (String name : input.columnNames()) {
                String tableName = tablenames[i];
                if(tableName.contains(".")){
                    tableName = tableName.substring(0, tableName.lastIndexOf('.'));
                }
                identifiers.add(new ColumnIdentifier(tableName, name));
            }
            columns.add(identifiers);
            close(input);
        }
        ArrayList<InclusionDependency> converted = new ArrayList<>();
        for (SimpleInd simpleInd : result) {
            ColumnPermutation left = createPermutation(simpleInd.left, columns);
            ColumnPermutation right = createPermutation(simpleInd.right, columns);
            InclusionDependency inclusionDependency = new InclusionDependency(left, right);
            //logger.info(inclusionDependency.toString());
            converted.add(inclusionDependency);
        }

        return converted;
    }

    private void close(RelationalInput input) {
        try {
            input.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private ColumnPermutation createPermutation(SimpleColumnCombination columnCombination, List<List<ColumnIdentifier>> columns) {
        ColumnIdentifier[] ids = new ColumnIdentifier[columnCombination.getColumns().length];
        List<ColumnIdentifier> identifier = columns.get(columnCombination.getTable());
        int j = 0;
        for (int i : columnCombination.getColumns()) {
            ids[j] = identifier.get(i); // ids[i] = identifier.get(i) throws out of bounce...
            j++;
        }
        return new ColumnPermutation(ids);
    }

}
