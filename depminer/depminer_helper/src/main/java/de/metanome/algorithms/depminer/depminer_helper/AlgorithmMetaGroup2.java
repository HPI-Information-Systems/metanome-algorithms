package de.metanome.algorithms.depminer.depminer_helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.FunctionalDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;

public abstract class AlgorithmMetaGroup2 implements FunctionalDependencyAlgorithm, RelationalInputParameterAlgorithm,
        BooleanParameterAlgorithm, StringParameterAlgorithm {

    public static String INPUT_TAG = "inputFile";
    public static String USE_OPTIMIZATIONS_TAG = "optimizations";

    protected FunctionalDependencyResultReceiver fdrr;
    protected Map<String, Object> configurationRequirements = new HashMap<String, Object>();
    protected ArrayList<ConfigurationRequirement> configSpecs = new ArrayList<ConfigurationRequirement>();

    @Override
    public ArrayList<ConfigurationRequirement> getConfigurationRequirements() {

        this.buildSpecs();
        return this.configSpecs;
    }

    @Override
    public void setStringConfigurationValue(String identifier, String... values) {

        this.configurationRequirements.put(identifier, values[0]);

    }

    @Override
    public void setBooleanConfigurationValue(String identifier, Boolean... values) {

        this.configurationRequirements.put(identifier, values[0]);

    }

    @Override
    public void execute() throws AlgorithmExecutionException, AlgorithmConfigurationException {

        this.executeAlgorithm();

    }

    @Override
    public void setResultReceiver(FunctionalDependencyResultReceiver resultReceiver) {

        this.fdrr = resultReceiver;

    }

    @Override
    public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) throws AlgorithmConfigurationException {

        this.configurationRequirements.put(identifier, values[0]);

    }

    protected abstract void buildSpecs();

    protected abstract void executeAlgorithm() throws AlgorithmConfigurationException, AlgorithmExecutionException;

}
