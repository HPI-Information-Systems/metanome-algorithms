package de.metanome.algorithms.depminer.depminer_algorithm;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithms.depminer.depminer_helper.AlgorithmMetaGroup2;

public class AlgorithmGroup2DepMiner extends AlgorithmMetaGroup2 {

    @Override
    protected void buildSpecs() {
    	configSpecs.add(new ConfigurationRequirementBoolean(AlgorithmMetaGroup2.USE_OPTIMIZATIONS_TAG));
        configSpecs.add(new ConfigurationRequirementRelationalInput(AlgorithmMetaGroup2.INPUT_TAG));
    }

    @Override
    protected void executeAlgorithm() throws AlgorithmConfigurationException, AlgorithmExecutionException {

        Object opti = this.configurationRequirements.get(AlgorithmMetaGroup2.USE_OPTIMIZATIONS_TAG);
        int numberOfThreads = 1;
        if (opti != null && (Boolean) opti) {
            numberOfThreads = Runtime.getRuntime().availableProcessors();
        }
        Object input = this.configurationRequirements.get(AlgorithmMetaGroup2.INPUT_TAG);
        if (input == null) {
            throw new AlgorithmConfigurationException("No input defined");
        }

        DepMiner dm = new DepMiner(numberOfThreads, this.fdrr);
        dm.execute(((RelationalInputGenerator) input).generateNewCopy());

    }

}
