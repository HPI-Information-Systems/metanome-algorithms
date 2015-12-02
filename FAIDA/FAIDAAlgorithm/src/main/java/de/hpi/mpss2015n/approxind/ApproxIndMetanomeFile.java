
package de.hpi.mpss2015n.approxind;

import com.google.common.base.Joiner;

import de.hpi.mpss2015n.approxind.inclusiontester.HLLInclusionTester;
import de.hpi.mpss2015n.approxind.sampler.IdentityRowSampler;
import de.hpi.mpss2015n.approxind.utils.Arity;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.FileInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.InclusionDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementFileInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.results.InclusionDependency;

import java.util.ArrayList;
import java.util.List;

public final class ApproxIndMetanomeFile implements InclusionDependencyAlgorithm, FileInputParameterAlgorithm,
        BooleanParameterAlgorithm, IntegerParameterAlgorithm {

    private InclusionDependencyResultReceiver resultReceiver;
    private FileInputGenerator[] fileInputGenerator;
    private String[] tableNames;
    private int inputRowLimit;
    private boolean detectNary;


    public enum Identifier {
        INPUT_FILES, INPUT_ROW_LIMIT, DETECT_NARY
    }

    @Override
    public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
        ArrayList<ConfigurationRequirement<?>> configs = new ArrayList<>();

        configs.add(new ConfigurationRequirementFileInput(Identifier.INPUT_FILES.name(), ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES));

        ConfigurationRequirementInteger inputRowlimit = new ConfigurationRequirementInteger(Identifier.INPUT_ROW_LIMIT.name());
        inputRowlimit.setRequired(false);
        configs.add(inputRowlimit);

        ConfigurationRequirementBoolean detectNary = new ConfigurationRequirementBoolean(Identifier.DETECT_NARY.name());
        detectNary.setDefaultValues(new Boolean[]{false});
        detectNary.setRequired(true);
        configs.add(detectNary);

        return configs;
    }

    @Override
    public void execute() throws AlgorithmExecutionException {

        boolean readExisting = false;
        ApproxIndAlgorithm algorithm = new ApproxIndAlgorithm(detectNary ? Arity.N_ARY : Arity.UNARY,
                                                              new IdentityRowSampler(), new HLLInclusionTester(0.001), readExisting);
        List<InclusionDependency> result = algorithm.execute(fileInputGenerator);

        for (InclusionDependency inclusionDependency : result) {
            resultReceiver.receiveResult(inclusionDependency);
        }

    }

    @Override
    public void setFileInputConfigurationValue(String identifier, FileInputGenerator... values) throws AlgorithmConfigurationException {
        if (Identifier.INPUT_FILES.name().equals(identifier)) {
            this.fileInputGenerator = values;

            this.tableNames = new String[values.length];
            for (int i = 0; i < values.length; i++)
                this.tableNames[i] = values[i].getInputFile().getName().split("\\.")[0];
        } else {
            this.handleUnknownConfiguration(identifier, Joiner.on(',').join(values));
        }
    }

    @Override
    public void setResultReceiver(InclusionDependencyResultReceiver resultReceiver) {
        this.resultReceiver = resultReceiver;
    }

    @Override
    public void setIntegerConfigurationValue(String identifier, Integer... values) throws AlgorithmConfigurationException {
        if (Identifier.INPUT_ROW_LIMIT.name().equals(identifier)) {
            if (values.length > 0)
                this.inputRowLimit = values[0];

        } else {
            this.handleUnknownConfiguration(identifier, Joiner.on(',').join(values));
        }
    }

    @Override
    public void setBooleanConfigurationValue(String identifier, Boolean... values) throws AlgorithmConfigurationException {
        if (Identifier.DETECT_NARY.name().equals(identifier)) {
            this.detectNary = values[0];
        } else {
            this.handleUnknownConfiguration(identifier, Joiner.on(',').join(values));
        }
    }

    protected void handleUnknownConfiguration(String identifier, String value) throws AlgorithmConfigurationException {
        throw new AlgorithmConfigurationException("Unknown configuration: " + identifier + " -> " + value);
    }

}
