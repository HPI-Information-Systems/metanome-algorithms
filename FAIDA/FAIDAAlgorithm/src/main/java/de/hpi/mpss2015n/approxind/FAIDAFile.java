
package de.hpi.mpss2015n.approxind;

import com.google.common.base.Joiner;
import de.hpi.mpss2015n.approxind.inclusiontester.HLLInclusionTester;
import de.hpi.mpss2015n.approxind.sampler.IdentityRowSampler;
import de.hpi.mpss2015n.approxind.utils.Arity;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.*;
import de.metanome.algorithm_integration.configuration.*;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.results.InclusionDependency;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;

public final class FAIDAFile implements InclusionDependencyAlgorithm, FileInputParameterAlgorithm,
        BooleanParameterAlgorithm, IntegerParameterAlgorithm, StringParameterAlgorithm {

    private InclusionDependencyResultReceiver resultReceiver;

    private FileInputGenerator[] fileInputGenerator;

    private String[] tableNames;

    private boolean isIgnoreNullColumns, isIgnoreConstantColumns;

    private boolean detectNary;

    private double hllRelativeStddev;

    private int sampleGoal;


    public enum Identifier {
        INPUT_FILES, DETECT_NARY, HLL_REL_STD_DEV, SAMPLE_GOAL, IGNORE_NULL, IGNORE_CONSTANT
    }

    @Override
    public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
        ArrayList<ConfigurationRequirement<?>> configs = new ArrayList<>();

        configs.add(new ConfigurationRequirementFileInput(Identifier.INPUT_FILES.name(), ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES));

        ConfigurationRequirementBoolean ignoreNullRequirement = new ConfigurationRequirementBoolean(Identifier.IGNORE_NULL.name());
        ignoreNullRequirement.setDefaultValues(new Boolean[] { true });
        ignoreNullRequirement.setRequired(true);
        configs.add(ignoreNullRequirement);

        ConfigurationRequirementBoolean ignoreConstantRequirement = new ConfigurationRequirementBoolean(Identifier.IGNORE_CONSTANT.name());
        ignoreConstantRequirement.setDefaultValues(new Boolean[] { true });
        ignoreConstantRequirement.setRequired(true);
        configs.add(ignoreConstantRequirement);

        ConfigurationRequirementBoolean detectNary = new ConfigurationRequirementBoolean(Identifier.DETECT_NARY.name());
        detectNary.setDefaultValues(new Boolean[]{false});
        detectNary.setRequired(true);
        configs.add(detectNary);

        ConfigurationRequirementString hllRelativeStandardDeviation = new ConfigurationRequirementString(Identifier.HLL_REL_STD_DEV.name());
        hllRelativeStandardDeviation.setDefaultValues(new String[]{"0.001"});
        hllRelativeStandardDeviation.setRequired(true);
        configs.add(hllRelativeStandardDeviation);

        ConfigurationRequirementInteger sampleGoal = new ConfigurationRequirementInteger(Identifier.SAMPLE_GOAL.name());
        sampleGoal.setDefaultValues(new Integer[]{500});
        sampleGoal.setRequired(true);
        configs.add(sampleGoal);

        return configs;
    }

    @Override
    public void execute() throws AlgorithmExecutionException {

        FAIDA algorithm = new FAIDA(
                detectNary ? Arity.N_ARY : Arity.UNARY,
                new IdentityRowSampler(),
                new HLLInclusionTester(this.hllRelativeStddev),
                sampleGoal,
                isIgnoreNullColumns,
                isIgnoreConstantColumns
        );
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
        if (Identifier.SAMPLE_GOAL.name().equals(identifier)) {
            Validate.inclusiveBetween(1, 1, values.length);
            this.sampleGoal = values[0];

        } else {
            this.handleUnknownConfiguration(identifier, Joiner.on(',').join(values));
        }
    }

    @Override
    public void setBooleanConfigurationValue(String identifier, Boolean... values) throws AlgorithmConfigurationException {
        if (Identifier.DETECT_NARY.name().equals(identifier)) {
            this.detectNary = values[0];
        } else if (Identifier.IGNORE_NULL.name().equals(identifier)) {
            this.isIgnoreNullColumns = values[0];
        } else if (Identifier.IGNORE_CONSTANT.name().equals(identifier)) {
            this.isIgnoreConstantColumns = values[0];
        } else {
            this.handleUnknownConfiguration(identifier, Joiner.on(',').join(values));
        }
    }

    @Override
    public void setStringConfigurationValue(String identifier, String... values) throws AlgorithmConfigurationException {
        if (Identifier.HLL_REL_STD_DEV.name().equals(identifier)) {
            Validate.inclusiveBetween(1, 1, values.length);
            this.hllRelativeStddev = Double.parseDouble(values[0]);
        } else {
            this.handleUnknownConfiguration(identifier, Joiner.on(',').join(values));
        }
    }

    protected void handleUnknownConfiguration(String identifier, String value) throws AlgorithmConfigurationException {
        throw new AlgorithmConfigurationException("Unknown configuration: " + identifier + " -> " + value);
    }

    @Override
    public String getAuthors() {
        return "Moritz Finke, Christian Dullweber, Martin Zabel, Manuel Hegner, Christian Zöllner";
    }

    @Override
    public String getDescription() {
        return "Approximate IND detection";
    }

}
