
package de.hpi.mpss2015n.approxind;

import com.google.common.base.Joiner;
import de.hpi.mpss2015n.approxind.datastructures.HyperLogLog;
import de.hpi.mpss2015n.approxind.inclusiontester.BloomFilterInclusionTester;
import de.hpi.mpss2015n.approxind.inclusiontester.BottomKSketchTester;
import de.hpi.mpss2015n.approxind.inclusiontester.CombinedHashSetInclusionTester;
import de.hpi.mpss2015n.approxind.inclusiontester.HLLInclusionTester;
import de.hpi.mpss2015n.approxind.sampler.IdentityRowSampler;
import de.hpi.mpss2015n.approxind.utils.Arity;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.*;
import de.metanome.algorithm_integration.configuration.*;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.results.InclusionDependency;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FAIDA implements InclusionDependencyAlgorithm, RelationalInputParameterAlgorithm,
        BooleanParameterAlgorithm, IntegerParameterAlgorithm, StringParameterAlgorithm {

    private InclusionDependencyResultReceiver resultReceiver;

    private RelationalInputGenerator[] inputGenerators;


    private boolean isIgnoreNullColumns = true,
            isIgnoreConstantColumns = true,
            isCombineNull = true,
            isUseVirtualColumnStore = false;

    private boolean detectNary = true;

    private String approximateTester = APPROXIMATE_TESTERS.get(0);

    private int approximateTesterBytes = 32 * 1024; // 32 KiB

    private static final List<String> APPROXIMATE_TESTERS = Arrays.asList(
            "HLL", "Bloom filter", "Bottom-k sketch", "Hash set"
    );

    private double hllRelativeStddev = 0.01;

    private int sampleGoal = 500;

    protected boolean isReuseColumnStore;


    public enum Identifier {
        INPUT_FILES, DETECT_NARY, APPROXIMATE_TESTER, APPROXIMATE_TESTER_BYTES, HLL_REL_STD_DEV, SAMPLE_GOAL,
        IGNORE_NULL, IGNORE_CONSTANT, REUSE_COLUMN_STORE, COMBINE_NULL, VIRTUAL_COLUMN_STORE
    }

    @Override
    public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
        ArrayList<ConfigurationRequirement<?>> configs = new ArrayList<>();

        configs.add(new ConfigurationRequirementRelationalInput(Identifier.INPUT_FILES.name(), ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES));

        ConfigurationRequirementString approximateTesterRequirement = new ConfigurationRequirementString(
                Identifier.APPROXIMATE_TESTER.name()
        );
        approximateTesterRequirement.setDefaultValues(new String[]{this.approximateTester});
        approximateTesterRequirement.setRequired(true);
        configs.add(approximateTesterRequirement);

        ConfigurationRequirementInteger approximateTesterBytesRequirement = new ConfigurationRequirementInteger(
                Identifier.APPROXIMATE_TESTER_BYTES.name()
        );
        approximateTesterBytesRequirement.setDefaultValues(new Integer[]{this.approximateTesterBytes});
        approximateTesterBytesRequirement.setRequired(false);
        configs.add(approximateTesterBytesRequirement);

        ConfigurationRequirementBoolean ignoreNullRequirement = new ConfigurationRequirementBoolean(Identifier.IGNORE_NULL.name());
        ignoreNullRequirement.setDefaultValues(new Boolean[]{this.isIgnoreNullColumns});
        ignoreNullRequirement.setRequired(true);
        configs.add(ignoreNullRequirement);

        ConfigurationRequirementBoolean combineNullRequirement = new ConfigurationRequirementBoolean(Identifier.COMBINE_NULL.name());
        ignoreNullRequirement.setDefaultValues(new Boolean[]{this.isCombineNull});
        ignoreNullRequirement.setRequired(true);
        configs.add(combineNullRequirement);

        ConfigurationRequirementBoolean ignoreConstantRequirement = new ConfigurationRequirementBoolean(Identifier.IGNORE_CONSTANT.name());
        ignoreConstantRequirement.setDefaultValues(new Boolean[]{this.isIgnoreConstantColumns});
        ignoreConstantRequirement.setRequired(true);
        configs.add(ignoreConstantRequirement);

        ConfigurationRequirementBoolean detectNary = new ConfigurationRequirementBoolean(Identifier.DETECT_NARY.name());
        detectNary.setDefaultValues(new Boolean[]{this.detectNary});
        detectNary.setRequired(true);
        configs.add(detectNary);

        ConfigurationRequirementBoolean reuseColumnStore = new ConfigurationRequirementBoolean(Identifier.REUSE_COLUMN_STORE.name());
        reuseColumnStore.setDefaultValues(new Boolean[]{this.isReuseColumnStore});
        reuseColumnStore.setRequired(false);
        configs.add(reuseColumnStore);

        ConfigurationRequirementBoolean virtualColumnStore = new ConfigurationRequirementBoolean(Identifier.VIRTUAL_COLUMN_STORE.name());
        virtualColumnStore.setDefaultValues(new Boolean[]{this.isUseVirtualColumnStore});
        virtualColumnStore.setRequired(false);
        configs.add(virtualColumnStore);

        ConfigurationRequirementString hllRelativeStandardDeviation = new ConfigurationRequirementString(Identifier.HLL_REL_STD_DEV.name());
        hllRelativeStandardDeviation.setDefaultValues(new String[]{Double.toString(this.hllRelativeStddev)});
        hllRelativeStandardDeviation.setRequired(false);
        configs.add(hllRelativeStandardDeviation);

        ConfigurationRequirementInteger sampleGoal = new ConfigurationRequirementInteger(Identifier.SAMPLE_GOAL.name());
        sampleGoal.setDefaultValues(new Integer[]{this.sampleGoal});
        sampleGoal.setRequired(true);
        configs.add(sampleGoal);

        return configs;
    }

    @Override
    public void execute() throws AlgorithmExecutionException {
        InclusionTester inclusionTester;
        if ("HLL".equalsIgnoreCase(this.approximateTester)) {
            inclusionTester = new HLLInclusionTester(this.hllRelativeStddev);
            System.out.printf("HLL with relative stddev of %.4f needs %,d bytes.\n",
                    this.hllRelativeStddev,
                    HyperLogLog.getRequiredCapacityInBytes(this.hllRelativeStddev));
        } else if ("Bloom filter".equalsIgnoreCase(this.approximateTester)) {
            inclusionTester = new BloomFilterInclusionTester(this.approximateTesterBytes);
        } else if ("Bottom-k sketch".equalsIgnoreCase(this.approximateTester)) {
            inclusionTester = new BottomKSketchTester(this.approximateTesterBytes);
        } else if ("Hash set".equalsIgnoreCase(this.approximateTester)) {
            inclusionTester = new CombinedHashSetInclusionTester();
        } else {
            throw new AlgorithmConfigurationException(String.format("Unknown tester: %s", this.approximateTester));
        }

        FAIDACore algorithm = new FAIDACore(
                detectNary ? Arity.N_ARY : Arity.UNARY,
                new IdentityRowSampler(),
                inclusionTester,
                sampleGoal,
                isIgnoreNullColumns,
                isIgnoreConstantColumns,
                isCombineNull,
                isUseVirtualColumnStore,
                isReuseColumnStore
        );
        List<InclusionDependency> result = algorithm.execute(inputGenerators);

        for (InclusionDependency inclusionDependency : result) {
            resultReceiver.receiveResult(inclusionDependency);
        }

    }

    @Override
    public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) throws AlgorithmConfigurationException {
        if (Identifier.INPUT_FILES.name().equals(identifier)) {
            if (values.length == 0) {
                throw new AlgorithmConfigurationException("No input files/tables given.");
            }
            this.inputGenerators = values.clone();
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
        } else if (Identifier.APPROXIMATE_TESTER_BYTES.name().equals(identifier)) {
            Validate.inclusiveBetween(1, 1, values.length);
            this.approximateTesterBytes = values[0];
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
        } else if (Identifier.COMBINE_NULL.name().equals(identifier)) {
            this.isCombineNull = values[0];
        } else if (Identifier.IGNORE_CONSTANT.name().equals(identifier)) {
            this.isIgnoreConstantColumns = values[0];
        } else if (Identifier.VIRTUAL_COLUMN_STORE.name().equals(identifier)) {
            this.isUseVirtualColumnStore = values[0];
        } else if (Identifier.REUSE_COLUMN_STORE.name().equals(identifier)) {
            this.isReuseColumnStore = values[0];
        } else {
            this.handleUnknownConfiguration(identifier, Joiner.on(',').join(values));
        }
    }

    @Override
    public void setStringConfigurationValue(String identifier, String... values) throws AlgorithmConfigurationException {
        if (Identifier.HLL_REL_STD_DEV.name().equals(identifier)) {
            Validate.inclusiveBetween(1, 1, values.length);
            this.hllRelativeStddev = Double.parseDouble(values[0]);
        } else if (Identifier.APPROXIMATE_TESTER.name().equals(identifier)) {
            Validate.inclusiveBetween(1, 1, values.length);
            this.approximateTester = values[0];
            if (!APPROXIMATE_TESTERS.contains(this.approximateTester)) {
                throw new AlgorithmConfigurationException(
                        String.format("Unknown tester: %s. Choose from %s.", this.approximateTester, APPROXIMATE_TESTERS)
                );
            }
        } else {
            this.handleUnknownConfiguration(identifier, Joiner.on(',').join(values));
        }
    }

    protected void handleUnknownConfiguration(String identifier, String value) throws AlgorithmConfigurationException {
        throw new AlgorithmConfigurationException("Unknown configuration: " + identifier + " -> " + value);
    }

    @Override
    public String getAuthors() {
        return "Moritz Finke, Christian Dullweber, Martin Zabel, Manuel Hegner, Christian Zöllner, Sebastian Kruse, Thorsten Papenbrock";
    }

    @Override
    public String getDescription() {
        return "Approximate IND detection";
    }

}
