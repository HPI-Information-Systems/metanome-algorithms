package de.metanome.algorithms.tireless;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BasicStatisticsAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementString;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.BasicStatisticsResultReceiver;

import java.util.ArrayList;

public class Tireless extends TirelessAlgorithm implements BasicStatisticsAlgorithm,
        IntegerParameterAlgorithm, StringParameterAlgorithm, RelationalInputParameterAlgorithm {

    @Override
    public void setResultReceiver(BasicStatisticsResultReceiver basicStatisticsResultReceiver) {
        this.resultReceiver = basicStatisticsResultReceiver;
    }

    @Override
    public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values)
            throws AlgorithmConfigurationException {
        if (!Tireless.Identifier.INPUT_GENERATOR.name().equals(identifier))
            throw new AlgorithmConfigurationException("Got an unexpected input generator.");
        this.inputGenerator = values[0];
    }

    @Override
    public String getAuthors() {
        return "tbd";
    }

    @Override
    public String getDescription() {
        return "tbd";
    }

    @Override
    public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
        ArrayList<ConfigurationRequirement<?>> conf = new ArrayList<>();
        conf.add(new ConfigurationRequirementRelationalInput(Tireless.Identifier.INPUT_GENERATOR.name()));

        appendMaximumElementCountConfiguration(conf);
        appendMinimalSpecialCharacterOccurrenceConfiguration(conf);
        appendDisjunctionMergingThresholdConfiguration(conf);
        appendMaximumLengthDeviationFactorConfiguration(conf);
        appendCharClassGeneralizationThresholdConfiguration(conf);
        appendQuantifierGeneralizationThresholdConfiguration(conf);
        appendOutlierThresholdConfiguration(conf);

        return conf;
    }

    private void appendMaximumElementCountConfiguration(ArrayList<ConfigurationRequirement<?>> conf) {
        ConfigurationRequirementInteger maximumElementCount =
                new ConfigurationRequirementInteger(Identifier.MAXIMUM_ELEMENT_COUNT.name());
        maximumElementCount.setDefaultValues(new Integer[]{30});
        maximumElementCount.setRequired(true);
        conf.add(maximumElementCount);
    }

    private void appendMinimalSpecialCharacterOccurrenceConfiguration(ArrayList<ConfigurationRequirement<?>> conf) {
        ConfigurationRequirementString minimalOccurrence =
                new ConfigurationRequirementString(Identifier.MINIMAL_SPECIAL_CHARACTER_OCCURRENCE.name());
        minimalOccurrence.setDefaultValues(new String[]{"0.2"});
        minimalOccurrence.setRequired(true);
        conf.add(minimalOccurrence);
    }

    private void appendDisjunctionMergingThresholdConfiguration(ArrayList<ConfigurationRequirement<?>> conf) {
        ConfigurationRequirementInteger disjunctionMergingThreshold =
                new ConfigurationRequirementInteger(Identifier.DISJUNCTION_MERGING_THRESHOLD.name());
        disjunctionMergingThreshold.setDefaultValues(new Integer[]{10});
        disjunctionMergingThreshold.setRequired(true);
        conf.add(disjunctionMergingThreshold);
    }

    private void appendMaximumLengthDeviationFactorConfiguration(ArrayList<ConfigurationRequirement<?>> conf) {
        ConfigurationRequirementString maximumLengthDeviationFactor =
                new ConfigurationRequirementString(Identifier.MAXIMUM_LENGTH_DEVIATION_FACTOR.name());
        maximumLengthDeviationFactor.setDefaultValues(new String[]{"4"});
        maximumLengthDeviationFactor.setRequired(true);
        conf.add(maximumLengthDeviationFactor);
    }

    private void appendCharClassGeneralizationThresholdConfiguration(ArrayList<ConfigurationRequirement<?>> conf) {
        ConfigurationRequirementInteger charClassGeneralizationThreshold =
                new ConfigurationRequirementInteger(Identifier.CHAR_CLASS_GENERALIZATION_THRESHOLD.name());
        charClassGeneralizationThreshold.setDefaultValues(new Integer[]{4});
        charClassGeneralizationThreshold.setRequired(true);
        conf.add(charClassGeneralizationThreshold);
    }

    private void appendQuantifierGeneralizationThresholdConfiguration(ArrayList<ConfigurationRequirement<?>> conf) {
        ConfigurationRequirementInteger charQuantifiersGeneralizationThreshold =
                new ConfigurationRequirementInteger(Identifier.QUANTIFIER_GENERALIZATION_THRESHOLD.name());
        charQuantifiersGeneralizationThreshold.setDefaultValues(new Integer[]{5});
        charQuantifiersGeneralizationThreshold.setRequired(true);
        conf.add(charQuantifiersGeneralizationThreshold);
    }

    private void appendOutlierThresholdConfiguration(ArrayList<ConfigurationRequirement<?>> conf) {
        ConfigurationRequirementString outlierThreshold =
                new ConfigurationRequirementString(Identifier.OUTLIER_THRESHOLD.name());
        outlierThreshold.setDefaultValues(new String[]{"0.05"});
        outlierThreshold.setRequired(true);
        conf.add(outlierThreshold);
    }

    @Override
    public void execute() throws AlgorithmExecutionException {
        super.execute();
    }

    @Override
    public void setIntegerConfigurationValue(String identifier, Integer... values)
            throws AlgorithmConfigurationException {
        if (Identifier.MAXIMUM_ELEMENT_COUNT.name().equals(identifier))
            this.maximumElementCount = values[0];
        else if (Identifier.DISJUNCTION_MERGING_THRESHOLD.name().equals(identifier))
            this.disjunctionMergingThreshold = values[0];
        else if (Identifier.CHAR_CLASS_GENERALIZATION_THRESHOLD.name().equals(identifier))
            this.charClassGeneralizationThreshold = values[0];
        else if (Identifier.QUANTIFIER_GENERALIZATION_THRESHOLD.name().equals(identifier))
            this.quantifierGeneralizationThreshold = values[0];
        else
            throw new AlgorithmConfigurationException("Got an unexpected configuration value.");
    }

    @Override
    public void setStringConfigurationValue(String identifier, String... values)
            throws AlgorithmConfigurationException {
        if (Identifier.MINIMAL_SPECIAL_CHARACTER_OCCURRENCE.name().equals(identifier))
            this.minimalSpecialCharacterOccurrence = Double.parseDouble(values[0]);
        else if (Identifier.MAXIMUM_LENGTH_DEVIATION_FACTOR.name().equals(identifier))
            this.maximumLengthDeviationFactor = Double.parseDouble(values[0]);
        else if (Identifier.OUTLIER_THRESHOLD.name().equals(identifier))
            this.outlierThreshold = Double.parseDouble(values[0]);
        else
            throw new AlgorithmConfigurationException("Got an unexpected configuration value.");
    }

    public enum Identifier {
        INPUT_GENERATOR, MAXIMUM_ELEMENT_COUNT, MINIMAL_SPECIAL_CHARACTER_OCCURRENCE, DISJUNCTION_MERGING_THRESHOLD,
        MAXIMUM_LENGTH_DEVIATION_FACTOR, CHAR_CLASS_GENERALIZATION_THRESHOLD, QUANTIFIER_GENERALIZATION_THRESHOLD,
        OUTLIER_THRESHOLD
    }
}
