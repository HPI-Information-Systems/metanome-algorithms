package de.metanome.algorithms.tireless;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.BasicStatisticsResultReceiver;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.results.BasicStatistic;
import de.metanome.algorithm_integration.results.basic_statistic_values.BasicStatisticValueString;
import de.metanome.algorithms.tireless.algorithm.RecursiveSubgroupDisjunctionAlgorithm;
import de.metanome.algorithms.tireless.postprocessing.CombinedPostprocessor;
import de.metanome.algorithms.tireless.postprocessing.ReduceElementCount;
import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.preprocessing.CharClasses;
import de.metanome.algorithms.tireless.preprocessing.InputReader;
import de.metanome.algorithms.tireless.preprocessing.StatisticsCollector;
import de.metanome.algorithms.tireless.preprocessing.alphabet.AlphabetNode;
import de.metanome.algorithms.tireless.preprocessing.alphabet.DefaultAlphabet;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionConjunction;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class TirelessAlgorithm {

    protected RelationalInputGenerator inputGenerator = null;
    protected BasicStatisticsResultReceiver resultReceiver = null;

    protected String relationName;
    protected List<String> columnNames;

    protected int maximumElementCount;
    protected double minimalSpecialCharacterOccurrence;
    protected int disjunctionMergingThreshold;
    protected double maximumLengthDeviationFactor;
    protected int charClassGeneralizationThreshold;
    protected int quantifierGeneralizationThreshold;
    protected double outlierThreshold;

    public void execute() throws AlgorithmExecutionException {

        List<Map<String, Integer>> columns = this.initialize();
        AlgorithmConfiguration configuration = getConfiguration();
        for(int i = 0; i < columns.size(); i++) {
            processColumn(columns, configuration, i);
        }
    }

    protected void processColumn(List<Map<String, Integer>> columns, AlgorithmConfiguration configuration, int i)
            throws CouldNotReceiveResultException, ColumnNameMismatchException {
        Map<String, Integer> column = columns.get(i);

        CharClasses charClasses = new CharClasses(column.keySet());
        BitSet excludedSpecials = new StatisticsCollector(column, charClasses.getSpecialCharClass(),
                configuration).getExcludedSpecials();
        AlphabetNode alphabet = DefaultAlphabet.getDefaultAlphabet(excludedSpecials, charClasses);

        RecursiveSubgroupDisjunctionAlgorithm algo =
                new RecursiveSubgroupDisjunctionAlgorithm(column, alphabet, configuration);
        RegularExpressionConjunction result = algo.computeExpression();
        if(result != null) {
            new CombinedPostprocessor(result, alphabet, configuration);
            ReduceElementCount reducer = new ReduceElementCount(result, configuration, alphabet);
            reducer.mergeNeighbours();
            emitResult(i, result.compile(configuration, alphabet));
        }
    }

    protected void emitResult(int columnIndex, String expression)
            throws CouldNotReceiveResultException, ColumnNameMismatchException {
        BasicStatistic bs = new BasicStatistic();
        bs.setColumnCombination(new ColumnCombination(
                new ColumnIdentifier(relationName, columnNames.get(columnIndex))));
        bs.addStatistic("regular expression", new BasicStatisticValueString(expression));
        resultReceiver.receiveResult(bs);
    }

    protected List<Map<String, Integer>> initialize()
            throws InputGenerationException, AlgorithmConfigurationException, InputIterationException {

        RelationalInput input = this.inputGenerator.generateNewCopy();
        this.relationName = input.relationName();
        this.columnNames = input.columnNames();
        return new InputReader(input).getValues();
    }

    protected AlgorithmConfiguration getConfiguration() {
        return new AlgorithmConfiguration(maximumElementCount, minimalSpecialCharacterOccurrence,
            disjunctionMergingThreshold, maximumLengthDeviationFactor, charClassGeneralizationThreshold,
                quantifierGeneralizationThreshold, outlierThreshold);
    }

}
