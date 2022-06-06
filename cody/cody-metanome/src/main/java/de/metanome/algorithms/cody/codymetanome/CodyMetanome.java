package de.metanome.algorithms.cody.codymetanome;

import de.metanome.algorithms.cody.codycore.Configuration;
import de.metanome.algorithms.cody.codycore.candidate.CheckedColumnCombination;
import de.metanome.algorithms.cody.codycore.runner.ApproximateRunner;
import de.metanome.algorithms.cody.codycore.runner.BaseRunner;
import de.metanome.algorithms.cody.codycore.runner.ExactRunner;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.ColumnPermutation;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.FileInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.InclusionDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementFileInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementString;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.results.InclusionDependency;

import java.util.ArrayList;
import java.util.List;

public class CodyMetanome implements StringParameterAlgorithm, BooleanParameterAlgorithm,
        FileInputParameterAlgorithm, InclusionDependencyAlgorithm {

    public static final String MIN_SUPPORT = "MIN_SUPPORT";
    public static final String NULL_VALUE = "NULL_VALUE";
    public static final String NO_CLIQUES = "NO_CLIQUES";
    public static final String INPUT_FILE = "INPUT_FILE";

    private double min_support = 1.0;
    private String null_value = "";
    private boolean no_cliques = false;
    private FileInputGenerator[] fileInputGenerators;
    private InclusionDependencyResultReceiver resultReceiver;

    @Override
    public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
        ArrayList<ConfigurationRequirement<?>> configs = new ArrayList<>();
        configs.add(new ConfigurationRequirementFileInput(INPUT_FILE));
        configs.add(new ConfigurationRequirementBoolean(NO_CLIQUES));
        ConfigurationRequirementString min_supp_conf = new ConfigurationRequirementString(MIN_SUPPORT);
        min_supp_conf.setDefaultValues(new String[] { "" + this.min_support });
        min_supp_conf.setRequired(false);
        configs.add(min_supp_conf);
        ConfigurationRequirementString null_conf = new ConfigurationRequirementString(NULL_VALUE);
        null_conf.setDefaultValues(new String[] { this.null_value });
        null_conf.setRequired(false);
        configs.add(null_conf);

        return configs;
    }

    @Override
    public void execute() throws AlgorithmExecutionException {
        try {
            Configuration config = new Configuration();
            config.setNoCliqueSearch(this.no_cliques);
            config.setMinSupport(this.min_support);

            for (FileInputGenerator file : this.fileInputGenerators) {
                config.setPath(file.getInputFile().getAbsolutePath());
                config.setNullValue(this.null_value);

                BaseRunner runner;
                if (this.min_support == 1.0) {
                    runner = new ExactRunner(config);
                } else {
                    runner = new ApproximateRunner(config);
                }
                runner.run();

                // convert resultSet to metanome formats
                List<String> columnNames = file.generateNewCopy().columnNames();
                String tableName = file.generateNewCopy().relationName();
                for (CheckedColumnCombination c : runner.getResultSet())
                    this.resultReceiver.receiveResult(this.translateToMetanome(c, tableName, columnNames));
            }
        } catch (Exception e) {
            throw new AlgorithmExecutionException(e.getMessage());
        }
    }

    @Override
    public String getAuthors() {
        return "Jonas Hering";
    }

    @Override
    public String getDescription() {
        return "Find Complementation Dependencies Using Compacted PLIs and a Bottom-Up--Top-Down Lattice Traversal " +
                "Strategy";
    }

    @Override
    public void setBooleanConfigurationValue(String identifier, Boolean... values) throws AlgorithmConfigurationException {
        if (NO_CLIQUES.equals(identifier)) {
            this.no_cliques = values[0];
            return;
        }

        throw new AlgorithmConfigurationException("Unknown parameter");
    }

    @Override
    public void setFileInputConfigurationValue(String identifier, FileInputGenerator... values) throws AlgorithmConfigurationException {
        if (INPUT_FILE.equals(identifier)) {
            this.fileInputGenerators = values;
            return;
        }

        throw new AlgorithmConfigurationException("Unknown parameter");
    }

    @Override
    public void setStringConfigurationValue(String identifier, String... values) throws AlgorithmConfigurationException {
        if (MIN_SUPPORT.equals(identifier)) {
            this.min_support = Double.parseDouble(values[0]);
            return;
        } else if (NULL_VALUE.equals(identifier)) {
            this.null_value = values[0];
            return;
        }

        throw new AlgorithmConfigurationException("Unknown parameter");
    }

    public void setResultReceiver(InclusionDependencyResultReceiver resultReceiver) {
        this.resultReceiver = resultReceiver;
    }

    private InclusionDependency translateToMetanome(CheckedColumnCombination c, String tableIdentifier,
                                                    List<String> columnIdentifiers) {
        ColumnIdentifier[] left = new ColumnIdentifier[c.getLeft().cardinality()];
        int index = 0;
        for (int i = c.getLeft().nextSetBit(0); i != -1; i = c.getLeft().nextSetBit(i + 1)) {
            left[index] = new ColumnIdentifier(tableIdentifier, columnIdentifiers.get(i));
            index++;
        }

        ColumnIdentifier[] right = new ColumnIdentifier[c.getRight().cardinality()];
        index = 0;
        for (int i = c.getRight().nextSetBit(0); i != -1; i = c.getRight().nextSetBit(i + 1)) {
            right[index] = new ColumnIdentifier(tableIdentifier, columnIdentifiers.get(i));
            index++;
        }

        return new InclusionDependency(new ColumnPermutation(left), new ColumnPermutation(right));
    }
}
