package de.metanome.algorithms.ducc;

import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.UniqueColumnCombinationsAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.UniqueColumnCombinationResultReceiver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class Ducc implements UniqueColumnCombinationsAlgorithm, RelationalInputParameterAlgorithm {
    protected static final String INPUT_HANDLE = "csvIterator";
    protected Random random;
    protected RelationalInputGenerator inputGenerator;
    protected UniqueColumnCombinationResultReceiver resultReceiver;

    public Ducc() {
        this.random = new Random();
    }

    @Override
    public ArrayList<ConfigurationRequirement> getConfigurationRequirements() {
        ArrayList<ConfigurationRequirement> spec = new ArrayList<>();
        ConfigurationRequirementRelationalInput csvFile = new ConfigurationRequirementRelationalInput(INPUT_HANDLE);
        spec.add(csvFile);
        return spec;
    }

    @Override
    public void setRelationalInputConfigurationValue(String identifier,
                                                     RelationalInputGenerator... values) throws AlgorithmConfigurationException {
        if (identifier.equals(INPUT_HANDLE)) {
            inputGenerator = values[0];
        } else {
            throw new AlgorithmConfigurationException("Operation should not be called");
        }
    }

    @Override
    public void execute() throws AlgorithmExecutionException {
        RelationalInput input;

        input = inputGenerator.generateNewCopy();

        PLIBuilder pliBuilder = new PLIBuilder(input);
        List<PositionListIndex> plis = pliBuilder.getPLIList();

        DuccAlgorithm duccAlgorithm = new DuccAlgorithm(input.relationName(), input.columnNames(), this.resultReceiver);
        duccAlgorithm.run(plis);
    }

    @Override
    public void setResultReceiver(UniqueColumnCombinationResultReceiver receiver) {
        this.resultReceiver = receiver;
    }
}