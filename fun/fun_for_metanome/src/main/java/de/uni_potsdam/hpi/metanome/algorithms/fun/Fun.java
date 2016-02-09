package de.uni_potsdam.hpi.metanome.algorithms.fun;

import java.util.ArrayList;
import java.util.List;

import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.algorithm_types.FunctionalDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;

public class Fun implements FunctionalDependencyAlgorithm, RelationalInputParameterAlgorithm {

    protected static final String INPUT_FILE_TAG = "Relational Input";

    protected RelationalInputGenerator inputGenerator;
    protected FunctionalDependencyResultReceiver resultReceiver;
    protected FunAlgorithm fun;


    @Override
    public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
        ArrayList<ConfigurationRequirement<?>> configurationSpecifications = new ArrayList<>();

        configurationSpecifications.add(new ConfigurationRequirementRelationalInput(INPUT_FILE_TAG));

        return configurationSpecifications;
    }

    @Override
    public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) {
        if (identifier.equals(INPUT_FILE_TAG)) {
            this.inputGenerator = values[0];
        }
    }

    @Override
    public void execute() throws InputGenerationException, InputIterationException, CouldNotReceiveResultException, AlgorithmConfigurationException, ColumnNameMismatchException {
        RelationalInput input = inputGenerator.generateNewCopy();
        PLIBuilder pliBuilder = new PLIBuilder(input);
        List<PositionListIndex> pliList = pliBuilder.getPLIList();

        this.fun = new FunAlgorithm(input.relationName(), input.columnNames(), this.resultReceiver);
        this.fun.run(pliList);

    }


    @Override
    public void setResultReceiver(FunctionalDependencyResultReceiver resultReceiver) {
        this.resultReceiver = resultReceiver;
    }

}
