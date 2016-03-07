package de.metanome.algorithms.fastfds;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithms.fastfds.fastfds_helper.AlgorithmMetaGroup2;

public class AlgorithmGroup2FastFD extends AlgorithmMetaGroup2 {

    @Override
    protected void buildSpecs() {
        configSpecs.add(new ConfigurationRequirementRelationalInput(AlgorithmMetaGroup2.INPUT_TAG));
        configSpecs.add(new ConfigurationRequirementBoolean(AlgorithmMetaGroup2.USE_OPTIMIZATIONS_TAG));
    }

    @Override
    protected void executeAlgorithm() throws AlgorithmConfigurationException, AlgorithmExecutionException {

        Object opti = this.configurationRequirements.get(AlgorithmMetaGroup2.USE_OPTIMIZATIONS_TAG);
        int numberOfThreads = 1;
        if (opti != null && (Boolean) opti) {
            // TODO: evtl. müssen wir hier noch mehr machen
            numberOfThreads = Runtime.getRuntime().availableProcessors();
        }
        Object input = this.configurationRequirements.get(AlgorithmMetaGroup2.INPUT_TAG);
        if (input == null) {
            throw new AlgorithmConfigurationException("No input defined");
        }

        FastFD ffd = new FastFD(numberOfThreads, this.fdrr);
        ffd.execute(((RelationalInputGenerator) input).generateNewCopy());

    }

    @Override
	public String getAuthors() {
		return FastFD.getAuthorName();
	}

	@Override
	public String getDescription() {
		return FastFD.getDescriptionText();
	}
}
