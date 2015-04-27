/**
 * @author Fabian Tschirschnitz
 */

package de.metanome.algorithms.anelosimus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.InclusionDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementString;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithms.anelosimus.bitvectors.BitVector;
import de.metanome.algorithms.anelosimus.bitvectors.BitVectorFactory;
import de.metanome.algorithms.anelosimus.bloom_filtering.BloomFilter;
import de.metanome.algorithms.anelosimus.filter.ColumnFilter;
import de.metanome.algorithms.anelosimus.helper.PrintHelper;
import de.metanome.algorithms.anelosimus.io.FileInputIterator;
import de.metanome.algorithms.anelosimus.io.InputIterator;

public class ANELOSIMUS implements InclusionDependencyAlgorithm, StringParameterAlgorithm, IntegerParameterAlgorithm, BooleanParameterAlgorithm, RelationalInputParameterAlgorithm {
    
	private static final double P_DIVISOR = 1000000000.0;
    Logger logger = LoggerFactory.getLogger(ANELOSIMUS.class);
    String timePattern = "MM/dd/yy HH:mm:ss";

    public enum N_EST_STRATEGIES {
        AVERAGE, AVERAGE_HALF_DOUBLED
    }

    public enum Identifier {
        TABLE_NAMES,
        RELATIONAL_INPUT,
        INPUT_ROW_LIMIT,
        M,
        K,
        P,
        N_EST_STRATEGY,
        STRATEGY_REF2DEPS,
        PASSES,
        DOP,
        VERIFY,
        FASTVECTOR,
        FILTER_NUMERIC_AND_SHORT_COLS,
        FILTER_DEPENDENT_REFS,
        FILTER_NON_UNIQUE_REFS,
        FILTER_NULL_COLS,
        CONDENSE_MATRIX,
        REF_COVERAGE_MIN_PERCENTAGE,
        NULL_VALUE_LIST,
        OUTPUT
    }

    public RelationalInputGenerator[] relationalInputGenerators;
    private int inputRowLimit;
    int[] tableColumnStartIndexes;
    String[] tableNames;
    List<String> columnNames;
    final List<List<String>> columnValueLists = new ArrayList<List<String>>();
    final Map<Integer, Set<String>> verificationCache = Collections
            .synchronizedMap(new HashMap<Integer, Set<String>>());
    int[] condensedMatrixMapping;

    N_EST_STRATEGIES strategy;
    int n_est;
    double p;
    int m;
    int k;
    int passes;
    int dop;
    int refMinCoverage;
    boolean verify;
    public boolean outputINDS;
    boolean filterNumericAndShortCols;
    boolean filterDependentRefs;
    boolean filterNonUniqueRefs;
    boolean filterNullCols;
    InclusionDependencyResultReceiver resultReceiver;
    Set<String> nullValues;
    boolean isStrategyRef2Deps;
    boolean isFastVector;
    boolean condenseMatrix;
    int matrixWidth;

    BitVectorFactory bitVectorFactory;

    int numColumns;
    BitVector<?> uniqeColumns;
    BitVector<?> unfilteredColumns;
    BitVector<?> nonNullColumns;
    BitVector<?> refCandidates;
    BitVector<?> dependentRefs;
    BitVector<?> depCandidates;
    BitVector<?> activeColumns;
    List<BitVector<?>> bitMatrix;
    BitVector<?> allZeros;
    BitVector<?> allOnes;
    AtomicLong numUnaryINDs = new AtomicLong();
    AtomicLong falsePositives = new AtomicLong();

    @Override
    public void execute() throws AlgorithmExecutionException {
        try {
            logger.debug("init start: {}", DateFormatUtils.formatUTC(System.currentTimeMillis(), timePattern));
            initialize();

            logger.debug("number of tables: {}", tableNames.length);
            logger.debug("bloom filtering start: {}",
                    DateFormatUtils.formatUTC(System.currentTimeMillis(), timePattern));
            bitVectorFactory = new BitVectorFactory(isFastVector);

            List<List<BloomFilter<String>>> allColumnsHashes = new ArrayList<>();
            uniqeColumns = bitVectorFactory.createBitVector(numColumns);
            unfilteredColumns = bitVectorFactory.createBitVector(numColumns);
            nonNullColumns = bitVectorFactory.createBitVector(numColumns);
            refCandidates = bitVectorFactory.createBitVector(numColumns).flip();
            // dependentRefs = new SynchronizedBitVector(bitVectorFactory.createBitVector(numColumns).flip());
            depCandidates = bitVectorFactory.createBitVector(numColumns).flip();
            if (p != 0) {
                logger.debug("Bloom filter length for n_est{}: {}({})", this.n_est, BloomFilter.getM(p, n_est),
                        BloomFilter.getK(p, n_est));
            }

            // iterate over columns
            for (int globalColumnIndex = 0; globalColumnIndex < columnValueLists.size(); globalColumnIndex++) {
                boolean skip = false;

                Set<String> columnSet = new HashSet<>(columnValueLists.get(globalColumnIndex));

                if (filterNullCols) {
                    if (!(columnSet.isEmpty() || (columnSet.size() == 1 && columnSet.contains(null)))) {
                        this.nonNullColumns.set(globalColumnIndex);
                    } else {
                        skip = true;
                    }
                }

                if (filterNumericAndShortCols) {
                    if (!ColumnFilter.INSTANCE.filterColumn(columnSet, nullValues)) {
                        this.unfilteredColumns.set(globalColumnIndex);
                    } else {
                        skip = true;
                    }
                }

                if (filterNonUniqueRefs && columnSet.size() == columnValueLists.get(globalColumnIndex).size()) {
                    this.uniqeColumns.set(globalColumnIndex);
                }

                List<BloomFilter<String>> bloomFilterList = new ArrayList<>();
                Validate.isTrue(passes <= 255);
                for (int j = 0; j < passes; j++) {
                    // take computed parameters
                    if (p != 0) {
                        bloomFilterList.add(new BloomFilter<String>(p, n_est, bitVectorFactory, (byte) j));
                        // take explicit params
                    } else {
                        bloomFilterList.add(new BloomFilter<String>(m, k, bitVectorFactory, (byte) j));
                    }

                }

                if (!skip) {
                    for (String value : columnSet) {
                        for (BloomFilter<String> bloomFilter : bloomFilterList) {
                            bloomFilter.add(value);
                        }
                    }
                }

                logger.trace("Bloom filter for column {}: {}", columnNames.get(globalColumnIndex), bloomFilterList);
                // add the Bloom Filters to our global collection
                allColumnsHashes.add(bloomFilterList);
                // remove the value list, since we do not need it anymore
                columnValueLists.set(globalColumnIndex, null);
                // put the value set into the cache
                verificationCache.put(globalColumnIndex, columnSet);
            }

            logger.debug("matrix generation, optional filtering: {}",
                    DateFormatUtils.formatUTC(System.currentTimeMillis(), timePattern));

            logger.debug("Unique columns: {}", uniqeColumns);
            logger.debug("Active columns: {}", unfilteredColumns);
            logger.debug("NonNull columns: {}", nonNullColumns);

            if (filterNonUniqueRefs) {
                refCandidates.and(uniqeColumns);
            }
            if (filterNumericAndShortCols) {
                refCandidates.and(unfilteredColumns);
            }
            if (filterNullCols) {
                refCandidates.and(nonNullColumns);
            }
            logger.debug("Ref candidates: {}", refCandidates);

            if (filterNumericAndShortCols) {
                depCandidates.and(unfilteredColumns);
            }
            if (filterNullCols) {
                depCandidates.and(nonNullColumns);
            }
            logger.debug("Dep candidates: {}", depCandidates);

            activeColumns = refCandidates.copy().or(depCandidates);

            if (logger.isDebugEnabled()) {
                logger.debug("Active columns:{}/{}", activeColumns.count(), numColumns);
            }

            if (condenseMatrix) {
                matrixWidth = activeColumns.count();
                condensedMatrixMapping = new int[matrixWidth];
                // our condensed pivot matrix
                bitMatrix = new ArrayList<BitVector<?>>();
                for (int row = 0; row < m * passes; row++) {
                    bitMatrix.add(bitVectorFactory.createBitVector(matrixWidth));
                }

                int lastActive = -1;
                for (int column = 0; column < matrixWidth; column++) {
                    // TODO check maybe?!
                    lastActive = activeColumns.next(lastActive);

                    // should never be reached
                    if (lastActive == -1) {
                        break;
                    }
                    condensedMatrixMapping[column] = lastActive;
                    for (int pass = 0; pass < passes; pass++) {
                        BloomFilter<String> bf = allColumnsHashes.get(lastActive).get(pass);
                        // iterate over bits in BF
                        final BitVector<?> bloomFilterIBits = bf.getBits();
                        // insert bits in pivot matrix
                        for (int j = 0; j < m; j++) {
                            if (bloomFilterIBits.get(j)) {
                                bitMatrix.get(j + pass * m).set(column);
                            }
                        }
                    }
                }
            } else {
                matrixWidth = numColumns;
                // our pivot matrix
                bitMatrix = new ArrayList<BitVector<?>>();
                for (int row = 0; row < m * passes; row++) {
                    bitMatrix.add(bitVectorFactory.createBitVector(matrixWidth));
                }

                for (int column = 0; column < matrixWidth; column++) {
                    for (int pass = 0; pass < passes; pass++) {
                        BloomFilter<String> bf = allColumnsHashes.get(column).get(pass);
                        // iterate over bits in BF
                        final BitVector<?> bloomFilterIBits = bf.getBits();
                        // insert bits in pivot matrix
                        for (int j = 0; j < m; j++) {
                            if (bloomFilterIBits.get(j)) {
                                bitMatrix.get(j + pass * m).set(column);
                            }
                        }
                    }

                }
            }

            allZeros = bitVectorFactory.createBitVector(matrixWidth);
            allOnes = bitVectorFactory.createBitVector(matrixWidth).flip();

            // some cleanup
            allColumnsHashes = null;
            System.gc();

            if (logger.isTraceEnabled()) {
                logger.trace("Matrix:\n{}", PrintHelper.printMatrix(bitMatrix));
            }

            if (isStrategyRef2Deps) {
                for (BitVector<?> row : bitMatrix) {
                    row.flip();
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Matrix inversed:\n{}", PrintHelper.printMatrix(bitMatrix));
                }
            }

            logger.debug("ind detection: {}",
                    DateFormatUtils.formatUTC(System.currentTimeMillis(), timePattern));

            final ExecutorService executor = Executors.newFixedThreadPool(dop);

            // let's divide the work
            final int nTasks = dop;
            for (int i = 0; i < nTasks; i++) {
                // chunk the load even
                final int colStart = (int) Math.floor(matrixWidth / nTasks) * i;
                int colEnd = (int) Math.floor(matrixWidth / nTasks) * (i + 1);
                if (i == (nTasks - 1)) {
                    colEnd = matrixWidth;
                }
                Runnable worker;

                worker = new INDDetectionWorker(this, colStart, colEnd, i);

                executor.execute(worker);
            }
            executor.shutdown();
            while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.debug("Awaiting completion of threads.");
            }

            logger.debug("ind done: {}",
                    DateFormatUtils.formatUTC(System.currentTimeMillis(), timePattern));
            logger.info("#uinds: {}", numUnaryINDs);
            logger.info("#fp: {}", falsePositives);
        } catch (Exception e) {
            logger.error(e.toString());
            throw new AlgorithmExecutionException(e.getMessage(), e);
        }

    }

    private void initialize() throws InputGenerationException, SQLException, InputIterationException {
        long overallRows = 0;

        // Ensure the presence of an input generator
        if (this.relationalInputGenerators == null) {
            throw new InputGenerationException("No input generator specified!");
        }

        // Query meta data for input tables
        tableColumnStartIndexes = new int[tableNames.length];
        columnNames = new ArrayList<String>();

        for (int tableIndex = 0; tableIndex < tableNames.length; tableIndex++) {
            this.tableColumnStartIndexes[tableIndex] = this.columnNames.size();

            RelationalInput input = this.relationalInputGenerators[tableIndex].generateNewCopy();

            storeColumnIdentifier(input);

            final int numTableColumns = input.numberOfColumns();

            final InputIterator inputIterator = new FileInputIterator(input,
                    inputRowLimit);

            List<List<String>> tableColumns = new ArrayList<>();
            for (int i = 0; i < numTableColumns; i++) {
                tableColumns.add(new ArrayList<String>());
            }

            // iterate over rows
            int rows = 0;
            while (inputIterator.next()) {
                for (int columnNumber = 0; columnNumber < numTableColumns; columnNumber++) {
                    String value = inputIterator.getValue(columnNumber);
                    // canonalize null values
                    if (nullValues != null) {
                        if (nullValues.contains(value)) {
                            value = null;
                        }
                    }

                    tableColumns.get(columnNumber).add(value);
                }
                rows++;
            }
            overallRows += rows;
            columnValueLists.addAll(tableColumns);

            try {
                inputIterator.close();
            } catch (Exception e) {
                logger.error("{}", e);
                e.printStackTrace();
            }
        }

        logger.trace("Column names: {}", columnNames);

        numColumns = this.columnNames.size();
        logger.debug("Number of columns: {}", numColumns);

        if (this.strategy.equals(N_EST_STRATEGIES.AVERAGE)) {
            this.n_est = (int) (overallRows / tableNames.length);
        } else if (this.strategy.equals(N_EST_STRATEGIES.AVERAGE_HALF_DOUBLED)) {
            this.n_est = (int) (overallRows / 2 / tableNames.length) * 2;
        }
        logger.debug("n estimation: {}", this.n_est);
    }

    private void storeColumnIdentifier(RelationalInput input) throws InputIterationException,
            InputGenerationException {
        // Query attribute name
        for (final String columnName : input.columnNames()) {
            this.columnNames.add(columnName);
        }
    }

    @Override
    public ArrayList<ConfigurationRequirement> getConfigurationRequirements() {
        final ArrayList<ConfigurationRequirement> configs = new ArrayList<ConfigurationRequirement>();
        configs.add(new ConfigurationRequirementRelationalInput(ANELOSIMUS.Identifier.RELATIONAL_INPUT.name(), ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES));
        
        // TODO: This is useless, because we can get the input names from their input generators: this.relationalInputGenerators[0].generateNewCopy().relationName()
        configs.add(new ConfigurationRequirementString(ANELOSIMUS.Identifier.TABLE_NAMES.name(), ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES));
        
        ConfigurationRequirementInteger m = new ConfigurationRequirementInteger(ANELOSIMUS.Identifier.M.name());
        Integer[] defaultM = new Integer[1];
        defaultM[0] = -1;
        m.setDefaultValues(defaultM);
		m.setRequired(true);
        configs.add(m);
        
        ConfigurationRequirementInteger k = new ConfigurationRequirementInteger(ANELOSIMUS.Identifier.K.name());
        Integer[] defaultK = new Integer[1];
        defaultK[0] = -1;
        k.setDefaultValues(defaultK);
		k.setRequired(true);
        configs.add(k);
        
        ConfigurationRequirementInteger p = new ConfigurationRequirementInteger(ANELOSIMUS.Identifier.P.name());
        Integer[] defaultP = new Integer[1];
        defaultP[0] = -1;
        p.setDefaultValues(defaultP);
		p.setRequired(true);
        configs.add(p);
        
        ConfigurationRequirementInteger passes = new ConfigurationRequirementInteger(ANELOSIMUS.Identifier.PASSES.name());
        Integer[] defaultPasses = new Integer[1];
        defaultPasses[0] = -1;
        passes.setDefaultValues(defaultPasses);
        passes.setRequired(true);
        configs.add(passes);
        
        ConfigurationRequirementInteger dop = new ConfigurationRequirementInteger(ANELOSIMUS.Identifier.DOP.name());
        Integer[] defaultDop = new Integer[1];
        defaultDop[0] = -1;
        dop.setDefaultValues(defaultDop);
		dop.setRequired(true);
        configs.add(dop);
        
        ConfigurationRequirementString nEstStrategy = new ConfigurationRequirementString(ANELOSIMUS.Identifier.N_EST_STRATEGY.name());
        String[] defaultNEstStrategy = new String[1];
        defaultNEstStrategy[0] = "???????";
        nEstStrategy.setDefaultValues(defaultNEstStrategy);
		nEstStrategy.setRequired(true);
        configs.add(nEstStrategy);
        
        ConfigurationRequirementBoolean filterNumericAndShortCols = new ConfigurationRequirementBoolean(ANELOSIMUS.Identifier.FILTER_NUMERIC_AND_SHORT_COLS.name());
        Boolean[] defaultFilterNumericAndShortCols = new Boolean[1];
        defaultFilterNumericAndShortCols[0] = true;
        filterNumericAndShortCols.setDefaultValues(defaultFilterNumericAndShortCols);
        filterNumericAndShortCols.setRequired(false);
        configs.add(filterNumericAndShortCols);
        
        ConfigurationRequirementBoolean filterDependentRefs = new ConfigurationRequirementBoolean(ANELOSIMUS.Identifier.FILTER_DEPENDENT_REFS.name());
        Boolean[] defaultFilterDependentRefs = new Boolean[1];
        defaultFilterDependentRefs[0] = true;
        filterDependentRefs.setDefaultValues(defaultFilterDependentRefs);
        filterDependentRefs.setRequired(false);
        configs.add(filterDependentRefs);
        
        ConfigurationRequirementBoolean filterNonUniqueRefs = new ConfigurationRequirementBoolean(ANELOSIMUS.Identifier.FILTER_NON_UNIQUE_REFS.name());
        Boolean[] defaultFilterNonUniqueRefs = new Boolean[1];
        defaultFilterNonUniqueRefs[0] = true;
        filterNonUniqueRefs.setDefaultValues(defaultFilterNonUniqueRefs);
        filterNonUniqueRefs.setRequired(false);
        configs.add(filterNonUniqueRefs);
        
        ConfigurationRequirementBoolean filterNullCols = new ConfigurationRequirementBoolean(ANELOSIMUS.Identifier.FILTER_NULL_COLS.name());
        Boolean[] defaultFilterNullCols = new Boolean[1];
        defaultFilterNullCols[0] = true;
        filterNullCols.setDefaultValues(defaultFilterNullCols);
        filterNullCols.setRequired(false);
        configs.add(filterNullCols);
        
        ConfigurationRequirementBoolean condenseMatrix = new ConfigurationRequirementBoolean(ANELOSIMUS.Identifier.CONDENSE_MATRIX.name());
        Boolean[] defaultCondenseMatrix = new Boolean[1];
        defaultCondenseMatrix[0] = true;
        condenseMatrix.setDefaultValues(defaultCondenseMatrix);
        condenseMatrix.setRequired(false);
        configs.add(condenseMatrix);
        
        ConfigurationRequirementInteger refCoverageMinPercentage = new ConfigurationRequirementInteger(ANELOSIMUS.Identifier.REF_COVERAGE_MIN_PERCENTAGE.name());
        Integer[] defaultRefCoverageMinPercentage = new Integer[1];
        defaultRefCoverageMinPercentage[0] = -1;
        refCoverageMinPercentage.setDefaultValues(defaultRefCoverageMinPercentage);
        refCoverageMinPercentage.setRequired(true);
        configs.add(refCoverageMinPercentage);
        
        ConfigurationRequirementBoolean strategyRef2Deps = new ConfigurationRequirementBoolean(ANELOSIMUS.Identifier.STRATEGY_REF2DEPS.name());
        Boolean[] defaultStrategyRef2Deps = new Boolean[1];
        defaultStrategyRef2Deps[0] = true;
        strategyRef2Deps.setDefaultValues(defaultStrategyRef2Deps);
        strategyRef2Deps.setRequired(false);
        configs.add(strategyRef2Deps);
        
        ConfigurationRequirementBoolean fastvector = new ConfigurationRequirementBoolean(ANELOSIMUS.Identifier.FASTVECTOR.name());
        Boolean[] defaultFastvector = new Boolean[1];
        defaultFastvector[0] = true;
        fastvector.setDefaultValues(defaultFastvector);
        fastvector.setRequired(false);
        configs.add(fastvector);
        
        // TODO: if this is a list of values that should be interpreted as NULL, then we could skip this parameter as well, because the NULL value is defined by Metanome per input source
        ConfigurationRequirementString nullValueList = new ConfigurationRequirementString(ANELOSIMUS.Identifier.NULL_VALUE_LIST.name());
        String[] defaultNullValueList = new String[1];
        defaultNullValueList[0] = "???????";
        nullValueList.setDefaultValues(defaultNullValueList);
        nullValueList.setRequired(true);
        configs.add(nullValueList);
        
        ConfigurationRequirementBoolean verify = new ConfigurationRequirementBoolean(ANELOSIMUS.Identifier.VERIFY.name());
        Boolean[] defaultVerify = new Boolean[1];
        defaultVerify[0] = true;
        verify.setDefaultValues(defaultVerify);
        verify.setRequired(false);
        configs.add(verify);
        
        // TODO: a better identifier could be "GENERATE_OUTPUT", but we can also remove this parameter, because Metanome is now able to simply count results and not store/remember them
        ConfigurationRequirementBoolean output = new ConfigurationRequirementBoolean(ANELOSIMUS.Identifier.OUTPUT.name());
        Boolean[] defaultOutput = new Boolean[1];
        defaultOutput[0] = true;
        output.setDefaultValues(defaultOutput);
        output.setRequired(false);
        configs.add(output);
        
        ConfigurationRequirementInteger inputRowLimit = new ConfigurationRequirementInteger(ANELOSIMUS.Identifier.INPUT_ROW_LIMIT.name());
        Integer[] defaultInputRowLimit = new Integer[1];
        defaultInputRowLimit[0] = -1;
        inputRowLimit.setDefaultValues(defaultInputRowLimit);
        inputRowLimit.setRequired(true);
        configs.add(inputRowLimit);

        return configs;
    }

    @Override
    public void setResultReceiver(InclusionDependencyResultReceiver resultReceiver) {
        this.resultReceiver = resultReceiver;
    }

    @Override
    public void setStringConfigurationValue(String identifier, String... values)
            throws AlgorithmConfigurationException {
        if (ANELOSIMUS.Identifier.TABLE_NAMES.name().equals(identifier)) {
            tableNames = values;
        } else if (ANELOSIMUS.Identifier.NULL_VALUE_LIST.name().equals(identifier)) {
            nullValues = new HashSet<String>(Arrays.asList(values));
        } else if (ANELOSIMUS.Identifier.N_EST_STRATEGY.name().equals(identifier)) {
            try {
                if (values[0] != null)
                    strategy = N_EST_STRATEGIES.valueOf(values[0]);
                else
                    strategy = N_EST_STRATEGIES.AVERAGE;
            } catch (IllegalArgumentException e) {
                throw new AlgorithmConfigurationException("Unknown configuration: " + identifier + " -> " + values);
            }
        }
        else {
            throw new AlgorithmConfigurationException("Unknown configuration: " + identifier + " -> " + values);
        }
    }

    @Override
    public void setRelationalInputConfigurationValue(String identifier,
            RelationalInputGenerator... values)
            throws AlgorithmConfigurationException {
        if (ANELOSIMUS.Identifier.RELATIONAL_INPUT.name().equals(identifier)) {
            this.relationalInputGenerators = values;
        } else {
            throw new AlgorithmConfigurationException("Unknown configuration: " + identifier + " -> " + values);
        }
    }

    @Override
    public void setBooleanConfigurationValue(String identifier, Boolean... values)
            throws AlgorithmConfigurationException {
        if (ANELOSIMUS.Identifier.VERIFY.name().equals(identifier)) {
            verify = values[0];
        } else if (ANELOSIMUS.Identifier.FILTER_NUMERIC_AND_SHORT_COLS.name().equals(identifier)) {
            filterNumericAndShortCols = values[0];
        } else if (ANELOSIMUS.Identifier.FILTER_DEPENDENT_REFS.name().equals(identifier)) {
            filterDependentRefs = values[0];
        } else if (ANELOSIMUS.Identifier.FILTER_NON_UNIQUE_REFS.name().equals(identifier)) {
            filterNonUniqueRefs = values[0];
        } else if (ANELOSIMUS.Identifier.FILTER_NULL_COLS.name().equals(identifier)) {
            filterNullCols = values[0];
        } else if (ANELOSIMUS.Identifier.STRATEGY_REF2DEPS.name().equals(identifier)) {
            isStrategyRef2Deps = values[0];
        } else if (ANELOSIMUS.Identifier.FASTVECTOR.name().equals(identifier)) {
            isFastVector = values[0];
        } else if (ANELOSIMUS.Identifier.CONDENSE_MATRIX.name().equals(identifier)) {
            condenseMatrix = values[0];
        } else if (ANELOSIMUS.Identifier.OUTPUT.name().equals(identifier)) {
            outputINDS = values[0];
        } else {
            throw new AlgorithmConfigurationException("Unknown configuration: " + identifier + " -> " + values);
        }
    }
    
    @Override
    public void setIntegerConfigurationValue(String identifier, Integer... values) throws AlgorithmConfigurationException {
    	if (values.length <= 0)
 			return;
    	if (ANELOSIMUS.Identifier.M.name().equals(identifier)) {
            m = values[0];
        } else if (ANELOSIMUS.Identifier.INPUT_ROW_LIMIT.name().equals(identifier)) {
            inputRowLimit = values[0];
        } else if (ANELOSIMUS.Identifier.K.name().equals(identifier)) {
            k = values[0];
        } else if (ANELOSIMUS.Identifier.P.name().equals(identifier)) {
            p = values[0] / P_DIVISOR;
        } else if (ANELOSIMUS.Identifier.PASSES.name().equals(identifier)) {
            passes = values[0];
        } else if (ANELOSIMUS.Identifier.DOP.name().equals(identifier)) {
            dop = values[0];
        } else if (ANELOSIMUS.Identifier.REF_COVERAGE_MIN_PERCENTAGE.name().equals(identifier)) {
            refMinCoverage = values[0];
        } else {
            throw new AlgorithmConfigurationException("Unknown configuration: " + identifier + " -> " + values);
        }
    }

    public String getTableNameFor(int column, int[] tableColumnStartIndexes) {
        for (int i = 1; i < tableColumnStartIndexes.length; i++) {
            if (tableColumnStartIndexes[i] > column) {
                return tableNames[i - 1];
            }
        }
        return tableNames[tableNames.length - 1];
    }

    public Set<String> getValueSetFor(int col) throws Exception {
        return this.verificationCache.get(col);
    }
}
