package de.metanome.algorithms.anelosimus.driver;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingFileInput;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithms.anelosimus.ANELOSIMUS;
import de.metanome.backend.input.file.DefaultFileInputGenerator;

public class AnelosimusDriver {
    {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE");
    }

    static Logger logger = LoggerFactory.getLogger(AnelosimusDriver.class);

    public static void main(String[] args) {
        AnelosimusParameters params = new AnelosimusParameters();
        new JCommander(params, args);

        String[] tableNames = listFiles(params.inputFolderPath + params.databaseName + File.separator,
                params.inputFileEnding, params.limit);

        InclusionDependencyResultReceiver resultReceiver = new SynchronizedDiscInclusionDependencyResultReceiver(
                params.outputFile);
        String[] nullValues = params.nullValues.toArray(new String[params.nullValues.size()]);

        try {
            run(
                    resultReceiver,
                    tableNames,
                    params.inputRowLimit,
                    params.nEstStrategy,
                    params.p,
                    params.m,
                    params.k,
                    params.passes,
                    nullValues,
                    params.dop,
                    params.refCoverageMinPercentage,
                    params.verify,
                    params.output,
                    params.filterNonUniqueRefs,
                    params.filterNullCols,
                    params.filterNumericAndShortCols,
                    params.filterDependentRefs,
                    params.useFastVector,
                    params.condenseMatrix,
                    params.strategyRef2Deps,
                    getRelationalInputs(params.databaseName, params.inputFileEnding, params.inputFolderPath,
                            params.hasHeader, params.seperator.charAt(0), params.quote.charAt(0),
                            params.escape.charAt(0),
                            tableNames));
        } catch (AlgorithmExecutionException e) {
            logger.error("{}", e);
            e.printStackTrace();
        }
        ((SynchronizedDiscInclusionDependencyResultReceiver) resultReceiver).close();
    }

    public static String[] listFiles(String dir, String inputFileEnding, int count) {
        final Set<String> matchingFileNames = new HashSet<String>();
        final File folder = new File(dir);
        if (!folder.exists()) {
            throw new IllegalArgumentException("No input folder found: " + folder.toString());
        }
        final File[] listOfFiles = folder.listFiles();

        for (int i = 0; ((count < 0) || (matchingFileNames.size() < count)) && (i < listOfFiles.length); i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(inputFileEnding)) {
                matchingFileNames.add(listOfFiles[i].getName().split("\\" + inputFileEnding)[0]);
            }
        }
        return matchingFileNames.toArray(new String[] {});
    }

    public static RelationalInputGenerator[] getRelationalInputs(String databaseName,
            String inputFileEnding,
            String inputFolderPath, boolean hasHeader, char seperator, char quote, char escape,
            String[] tableNames) {
        final RelationalInputGenerator[] fileInputGenerators = new FileInputGenerator[tableNames.length];
        if (escape == '0') {
            escape = '\0';
        }
        try {
            for (int i = 0; i < tableNames.length; i++) {
                fileInputGenerators[i] = new DefaultFileInputGenerator(new ConfigurationSettingFileInput(
                        inputFolderPath + databaseName + File.separator + tableNames[i] + inputFileEnding, true,
                        seperator,
                        quote, escape, true, true, 0, hasHeader, true, ""));
            }
            return fileInputGenerators;
        } catch (AlgorithmExecutionException e) {
            logger.error("{}", e);
            e.printStackTrace();
        }
        return null;

    }

    public static void run(InclusionDependencyResultReceiver resultReceiver, String[] tableNames, int inputRowLimit,
            String n_est_strategy, int p, int m, int k, int passes, String[] nullValues, int dop,
            int refCoverageMinPercentage, boolean verify, boolean output, boolean filterNonUniqueRefs,
            boolean filterNullCols, boolean filterNumericAndShortCols, boolean filterDependentRefs,
            boolean useFastVector, boolean condenseMatrix, boolean strategyRef2Deps,
            final RelationalInputGenerator[] fileInputGenerators) throws AlgorithmConfigurationException,
            AlgorithmExecutionException {
        ANELOSIMUS anelosimus = new ANELOSIMUS();
        anelosimus.setRelationalInputConfigurationValue(ANELOSIMUS.Identifier.INPUT_GENERATORS.name(),
                fileInputGenerators);

        anelosimus.setResultReceiver(resultReceiver);

        anelosimus.setIntegerConfigurationValue(ANELOSIMUS.Identifier.INPUT_ROW_LIMIT.name(), inputRowLimit);

        anelosimus.setStringConfigurationValue(ANELOSIMUS.Identifier.TABLE_NAMES.name(), tableNames);
        anelosimus.setStringConfigurationValue(ANELOSIMUS.Identifier.NULL_VALUE_LIST.name(), nullValues);
        anelosimus.setStringConfigurationValue(ANELOSIMUS.Identifier.N_EST_STRATEGY.name(), n_est_strategy);
        anelosimus
                .setIntegerConfigurationValue(ANELOSIMUS.Identifier.P.name(), p);
        anelosimus
                .setIntegerConfigurationValue(ANELOSIMUS.Identifier.K.name(), k);
        anelosimus.setIntegerConfigurationValue(ANELOSIMUS.Identifier.M.name(), m);
        anelosimus.setIntegerConfigurationValue(ANELOSIMUS.Identifier.DOP.name(), dop);
        anelosimus.setIntegerConfigurationValue(ANELOSIMUS.Identifier.PASSES.name(), passes);
        anelosimus.setBooleanConfigurationValue(ANELOSIMUS.Identifier.VERIFY.name(), verify);
        anelosimus.setBooleanConfigurationValue(ANELOSIMUS.Identifier.OUTPUT.name(), output);

        anelosimus.setBooleanConfigurationValue(ANELOSIMUS.Identifier.FILTER_NON_UNIQUE_REFS.name(),
                filterNonUniqueRefs);

        anelosimus.setIntegerConfigurationValue(ANELOSIMUS.Identifier.REF_COVERAGE_MIN_PERCENTAGE.name(),
                refCoverageMinPercentage);

        anelosimus.setBooleanConfigurationValue(ANELOSIMUS.Identifier.FILTER_NULL_COLS.name(), filterNullCols);

        anelosimus.setBooleanConfigurationValue(ANELOSIMUS.Identifier.FILTER_NUMERIC_AND_SHORT_COLS.name(),
                filterNumericAndShortCols);

        anelosimus.setBooleanConfigurationValue(ANELOSIMUS.Identifier.FILTER_DEPENDENT_REFS.name(),
                filterDependentRefs);
        anelosimus.setBooleanConfigurationValue(ANELOSIMUS.Identifier.FASTVECTOR.name(),
                useFastVector);
        anelosimus.setBooleanConfigurationValue(ANELOSIMUS.Identifier.CONDENSE_MATRIX.name(),
                condenseMatrix);

        anelosimus.setBooleanConfigurationValue(ANELOSIMUS.Identifier.STRATEGY_REF2DEPS.name(), strategyRef2Deps);

        long start = System.currentTimeMillis();

        logger.debug("start: {}", DateFormatUtils.formatUTC(start, "MM/dd/yy HH:mm:ss"));
        anelosimus.execute();
        logger.debug("end: {}", DateFormatUtils.formatUTC(System.currentTimeMillis(), "MM/dd/yy HH:mm:ss"));
        logger.info("took: {} seconds", (System.currentTimeMillis() - start) / 1000f);
    }

    /*
     * public static void runAnelosimus(InclusionDependencyResultReceiver resultReceiver, RelationalInputGenerator[]
     * fileInputGenerators, String n_est_strategy, int p, int m, int k, int passes, String[] nullValues, int dop, int
     * refCoverageMinPercentage, boolean verify, boolean filterNonUniqueRefs, boolean filterNullCols, boolean
     * filterNumericAndShortCols, boolean filterDependentRefs, boolean useFastVector, boolean condenseMatrix, boolean
     * strategyRef2Deps) { String[] tableNames = new String[fileInputGenerators.length]; try { int i = 0; for
     * (RelationalInputGenerator gen : fileInputGenerators) { tableNames[i] = gen.generateNewCopy().relationName(); i++;
     * } run(resultReceiver, tableNames, n_est_strategy, p, m, k, passes, nullValues, dop, refCoverageMinPercentage,
     * verify, filterNonUniqueRefs, filterNullCols, filterNumericAndShortCols, filterDependentRefs, useFastVector,
     * condenseMatrix, strategyRef2Deps, fileInputGenerators); } catch (AlgorithmExecutionException e) {
     * logger.error("{}", e); e.printStackTrace(); } }
     */
    /*
     * public static void runAnelosimus(InclusionDependencyResultReceiver resultReceiver, String databaseName, String
     * inputFileEnding, String inputFolderPath, boolean hasHeader, char seperator, char quote, char escape, String[]
     * tableNames, String n_est_strategy, int p, int m, int k, int passes, String[] nullValues, int dop, int
     * refCoverageMinPercentage, boolean verify, boolean filterNonUniqueRefs, boolean filterNullCols, boolean
     * filterNumericAndShortCols, boolean filterDependentRefs, boolean useFastVector, boolean condenseMatrix, boolean
     * strategyRef2Deps) { final RelationalInputGenerator[] fileInputGenerators = new
     * FileInputGenerator[tableNames.length]; try { for (int i = 0; i < tableNames.length; i++) { fileInputGenerators[i]
     * = new DefaultFileInputGenerator(new ConfigurationSettingFileInput( inputFolderPath + databaseName +
     * File.separator + tableNames[i] + inputFileEnding, true, seperator, quote, escape, true, true, 0, hasHeader,
     * true)); } run(resultReceiver, tableNames, n_est_strategy, p, m, k, passes, nullValues, dop,
     * refCoverageMinPercentage, verify, filterNonUniqueRefs, filterNullCols, filterNumericAndShortCols,
     * filterDependentRefs, useFastVector, condenseMatrix, strategyRef2Deps, fileInputGenerators); } catch
     * (AlgorithmExecutionException e) { logger.error("{}", e); e.printStackTrace(); } }
     */
}
