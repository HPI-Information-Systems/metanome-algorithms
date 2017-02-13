package de.hpi.mpss2015n.approxind;

import com.google.common.base.Stopwatch;
import de.hpi.mpss2015n.approxind.utils.*;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.results.InclusionDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public final class FAIDACore {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final boolean detectNary;
    private final RowSampler sampler;
    private final InclusionTester inclusionTester;
    private final CandidateGenerator candidateLogic;
    private final boolean ignoreNullValueColumns;
    private final boolean ignoreAllConstantColumns;
    private final boolean isCombineNull;
    private final boolean isUseVirtualColumnStore;
    private final boolean isReuseColumnStore;
    private final int sampleGoal;

    public FAIDACore(Arity arity, RowSampler sampler, InclusionTester inclusionTester, int sampleGoal) {
        this(arity, sampler, inclusionTester, sampleGoal, true, true, true, false, false);
    }

    public FAIDACore(Arity arity, RowSampler sampler, InclusionTester inclusionTester, int sampleGoal,
                     boolean ignoreNullValueColumns, boolean ignoreAllConstantColumns, boolean isCombineNull,
                     boolean isUseVirtualColumnStore, boolean isReuseColumnStore) {
        this.detectNary = arity == Arity.N_ARY;
        this.sampler = sampler;
        this.inclusionTester = inclusionTester;
        this.candidateLogic = new CandidateGenerator();
        this.sampleGoal = sampleGoal;
        this.ignoreNullValueColumns = ignoreNullValueColumns;
        this.ignoreAllConstantColumns = ignoreAllConstantColumns;
        this.isCombineNull = isCombineNull;
        this.isUseVirtualColumnStore = isUseVirtualColumnStore;
        this.isReuseColumnStore = isReuseColumnStore;
    }

    public List<InclusionDependency> execute(RelationalInputGenerator[] fileInputGenerators)
            throws InputGenerationException, InputIterationException, AlgorithmConfigurationException {
        IndConverter converter = new IndConverter(fileInputGenerators);
        List<SimpleInd> result = executeInternal(fileInputGenerators);
        logger.info("Result size: {}", result.size());
        logger.info("Certain checks: {}, uncertain checks: {}",
                this.inclusionTester.getNumCertainChecks(),
                this.inclusionTester.getNumUnertainChecks()
        );

        int i = 0;
        String[] tableNames = new String[fileInputGenerators.length];
        for (RelationalInputGenerator input : fileInputGenerators) {
            tableNames[i] = input.generateNewCopy().relationName();
            i++;
        }
        return converter.toMetanomeInds(result, tableNames);
    }


    List<SimpleInd> executeInternal(RelationalInputGenerator[] fileInputGenerators)
            throws InputGenerationException, InputIterationException, AlgorithmConfigurationException {
        // Probably for row scalability tests... not the sample described in the paper.
        fileInputGenerators = sampler.createSample(fileInputGenerators);
        int arity = 1;

        logger.info("Creating column stores.");
        AbstractColumnStore[] stores = this.isUseVirtualColumnStore ?
                VirtualColumnStore.create(fileInputGenerators, this.sampleGoal, this.isReuseColumnStore) :
                HashedColumnStore.create(fileInputGenerators, this.sampleGoal, this.isReuseColumnStore);
        int constantColumnCounter = 0, nullColumnCounter = 0;
        for (AbstractColumnStore store : stores) {
            for (int columnIndex = 0; columnIndex < store.getNumberOfColumns(); columnIndex++) {
                if (store.isConstantColumn(columnIndex)) constantColumnCounter++;
                else if (store.isNullColumn(columnIndex)) nullColumnCounter++;
            }
        }
        logger.info("Detected {} null columns and {} constant columns.", nullColumnCounter, constantColumnCounter);

        logger.info("Creating initial column combinations.");
        List<SimpleColumnCombination> combinations = createUnaryColumnCombinations(stores);
        logger.info("Created {} column combinations.", combinations.size());

        logger.info("Creating unary IND candidates.");
        List<SimpleInd> candidates = createUnaryIndCandidates(combinations);
        logger.info("Created {} IND candidates.", candidates.size());

        logger.info("Feeding input rows to IND test.");
        int[] tables = inclusionTester.setColumnCombinations(combinations);
        insertRows(tables, stores);

        logger.info("Checking unary IND candidates.");
        List<SimpleInd> result = checkCandidates(candidates);

        List<SimpleInd> lastResult = result;
        if (detectNary) {
            while (lastResult.size() > 0) {
                arity++;
                logger.info("Creating {}-ary IND candidates.", arity);
                candidates = candidateLogic.createCombinedCandidates(lastResult, isCombineNull, stores);
                if (candidates.isEmpty()) {
                    logger.info("no more candidates for next level!");
                    break;
                }
                logger.info("Created {} {}-ary IND candidates.", candidates.size(), arity);

                logger.info("Extracting {}-ary column combinations.", arity);
                combinations = extractColumnCombinations(candidates);

                logger.info("Inserting rows to check {} {}-ary IND candidates with {} column combinations", candidates.size(), arity,
                        combinations.size());
                int[] activeTables = inclusionTester.setColumnCombinations(combinations);
                insertRows(activeTables, stores);

                logger.info("Checking {}-ary IND candidates.", arity);
                lastResult = checkCandidates(candidates);
                result.addAll(lastResult);
            }
        }

        return result;
    }

    private void insertRows(int[] activeTables, AbstractColumnStore[] stores)
            throws InputGenerationException, InputIterationException {
        Stopwatch sw = Stopwatch.createStarted();

        List<List<long[]>> samples = new ArrayList<>();
        for (AbstractColumnStore store : stores) {
            samples.add(store.getSampleFile());
        }
        inclusionTester.initialize(samples);

        for (int table : activeTables) {
            int rowCount = 0;
            AbstractColumnStore inputGenerator = stores[table];
            ColumnIterator input = inputGenerator.getRows();
            logger.info("Inserting rows for table {}", table);
            DebugCounter counter = new DebugCounter();
            inclusionTester.startInsertRow(table);
            while (input.hasNext()) {
                inclusionTester.insertRow(input.next(), rowCount);
                rowCount++;
                counter.countUp();
            }
            counter.done();
            logger.info("{} rows inserted", rowCount);
            input.close();
        }
        inclusionTester.finalizeInsertion();
        logger.info("Time processing rows: {}ms", sw.elapsed(TimeUnit.MILLISECONDS));
    }

    private List<SimpleInd> checkCandidates(List<SimpleInd> candidates) {
        Stopwatch sw = Stopwatch.createStarted();
        List<SimpleInd> result = new ArrayList<>();
        logger.info("checking: {} candidates on level {}", candidates.size(),
                candidates.get(0).size());
        int candidateCount = 0;
        for (SimpleInd candidate : candidates) {
            if (inclusionTester.isIncludedIn(candidate.left, candidate.right)) {
                result.add(candidate); // add result
            }
            candidateCount++;
            if ((candidates.size() > 1000 && candidateCount % (candidates.size() / 20) == 0) ||
                    (candidates.size() <= 1000 && candidateCount % 100 == 0)) {
                logger.info("{}/{} candidates checked", candidateCount, candidates.size());
            }
        }
        logger.info("Time checking candidates on level {}: {}ms, INDs found: {}",
                candidates.get(0).size(),
                sw.elapsed(TimeUnit.MILLISECONDS), result.size());
        return result;
    }

    /**
     * Creates unary column combinations, thereby removing null columns and constant columns if requested.
     */
    public List<SimpleColumnCombination> createUnaryColumnCombinations(AbstractColumnStore[] stores) {
        List<SimpleColumnCombination> combinations = new ArrayList<>();
        int index = 0;
        for (int table = 0; table < stores.length; table++) {
            final AbstractColumnStore store = stores[table];
            int numColumns = store.getNumberOfColumns();

            for (int column = 0; column < numColumns; column++) {
                if (ignoreAllConstantColumns && store.isConstantColumn(column)) {
                    continue;
                }
                if (ignoreNullValueColumns && store.isNullColumn(column)) {
                    continue;
                }

                SimpleColumnCombination combination = SimpleColumnCombination.create(table, column);
                combination.setIndex(index);
                combinations.add(combination);
            }
        }
        return combinations;
    }

    /**
     * Create all possible IND candidates from the given column combinations.
     */
    private List<SimpleInd> createUnaryIndCandidates(List<SimpleColumnCombination> combinations) {
        List<SimpleInd> candidates = new ArrayList<>();
        for (SimpleColumnCombination left : combinations) {
            for (SimpleColumnCombination right : combinations) {
                if (!left.equals(right)) {
                    candidates.add(new SimpleInd(left, right));
                }
            }
        }
        return candidates;
    }

    /**
     * Extract all column combinations from the given IND (candidates).
     */
    private List<SimpleColumnCombination> extractColumnCombinations(List<SimpleInd> candidates) {
        Set<SimpleColumnCombination> combinations = new HashSet<>();
        for (SimpleInd candidate : candidates) {
            combinations.add(candidate.left);
            combinations.add(candidate.right);
        }
        return new ArrayList<>(combinations);
    }
}
