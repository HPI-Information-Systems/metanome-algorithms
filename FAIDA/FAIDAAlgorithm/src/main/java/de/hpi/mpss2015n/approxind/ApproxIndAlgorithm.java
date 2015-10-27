package de.hpi.mpss2015n.approxind;

import com.google.common.base.Stopwatch;

import de.hpi.mpss2015n.approxind.utils.Arity;
import de.hpi.mpss2015n.approxind.utils.CandidateGenerator;
import de.hpi.mpss2015n.approxind.utils.ColumnIterator;
import de.hpi.mpss2015n.approxind.utils.ColumnStore;
import de.hpi.mpss2015n.approxind.utils.DebugCounter;
import de.hpi.mpss2015n.approxind.utils.IndConverter;
import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;
import de.hpi.mpss2015n.approxind.utils.SimpleInd;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.results.InclusionDependency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public final class ApproxIndAlgorithm {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final boolean detectNary;
  private final RowSampler sampler;
  private final InclusionTester inclusionTester;
  private final CandidateGenerator candidateLogic;
  private final boolean readExisting;
  private final boolean ignoreNullValueColumns;
  private final boolean ignoreAllConstantColumns;

  public ApproxIndAlgorithm(Arity arity, RowSampler sampler, InclusionTester inclusionTester,
                            boolean readExisting) {
    this(arity, sampler, inclusionTester, readExisting, true, true);
  }
  
  public ApproxIndAlgorithm(Arity arity, RowSampler sampler, InclusionTester inclusionTester,
                            boolean readExisting, boolean ignoreNullValueColumns, boolean ignoreAllConstantColumns) {
    this.detectNary = arity == Arity.N_ARY;
    this.sampler = sampler;
    this.inclusionTester = inclusionTester;
    this.candidateLogic = new CandidateGenerator();
    this.readExisting = readExisting;
    this.ignoreNullValueColumns=ignoreNullValueColumns;
    this.ignoreAllConstantColumns=ignoreAllConstantColumns;
  }

  public List<InclusionDependency> execute(RelationalInputGenerator[] fileInputGenerators)
      throws InputGenerationException, InputIterationException {
    IndConverter converter = new IndConverter(fileInputGenerators);
    List<SimpleInd> result = executeInternal(fileInputGenerators);
    logger.info("Result size: {}", result.size());

    int i = 0;
    String[] tableNames = new String[fileInputGenerators.length];
    for (RelationalInputGenerator input : fileInputGenerators) {
      tableNames[i] = input.generateNewCopy().relationName();
      i++;
    }
    return converter.toMetanomeInds(result, tableNames);
  }


  List<SimpleInd> executeInternal(RelationalInputGenerator[] fileInputGenerators)
      throws InputGenerationException, InputIterationException {
    fileInputGenerators = sampler.createSample(fileInputGenerators);

    ColumnStore[] stores = ColumnStore.create(fileInputGenerators, readExisting, 10000);

    List<SimpleColumnCombination> nullColumns = new ArrayList<>();
    int constantColumnCounter = 0;
    for (ColumnStore store : stores) {
      nullColumns.addAll(store.getNullColumns());

      for(boolean bol : store.getIsConstantColumn()){
        if(bol){
          constantColumnCounter++;
        }
      }
    }
    logger.info(constantColumnCounter + " constant columns were detected - including " + nullColumns.size() + " null columns");

    List<SimpleColumnCombination> combinations = createInitialCombinations(stores);

    List<SimpleInd> candidates = createInitialCandidates(combinations);

    int[] tables = inclusionTester.setColumnCombinations(combinations);

    insertRows(tables, stores);
    List<SimpleInd> result = checkCandidates(candidates);

    List<SimpleInd> lastResult = result;
    if (detectNary) {
      while (lastResult.size() > 0) {
        candidates = candidateLogic.createCombinedCandidates(lastResult);
        if (candidates.size() == 0) {
          logger.info("no more candidates for next level!");
          break;
        }
        combinations = getCombinations(candidates);
        logger.info("checking {} candidates with {} combinations", candidates.size(),
                    combinations.size());
        int[] activeTables = inclusionTester.setColumnCombinations(combinations);
        insertRows(activeTables, stores);
        lastResult = checkCandidates(candidates);
        result.addAll(lastResult);
      }
    }

    return result;
  }

  private void insertRows(int[] activeTables, ColumnStore[] stores)
      throws InputGenerationException, InputIterationException {
    Stopwatch sw = Stopwatch.createStarted();

    List<List<long[]>> samples = new ArrayList<>();
    for(ColumnStore store : stores){
      samples.add(store.getSample());
    }
    inclusionTester.initialize(samples);

    for (int table : activeTables) {
      int rowCount = 0;
      ColumnStore inputGenerator = stores[table];
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
      if ((candidates.size()>1000 && candidateCount % (candidates.size()/20) == 0) || 
    		(candidates.size()<=1000 && candidateCount %100 == 0)) {
        logger.info("{}/{} candidates checked", candidateCount, candidates.size());
      }
    }
    logger.info("Time checking candidates on level {}: {}ms, INDs found: {}",
                candidates.get(0).size(),
                sw.elapsed(TimeUnit.MILLISECONDS), result.size());
    return result;
  }

  private List<SimpleInd> createInitialCandidates(List<SimpleColumnCombination> combinations) {
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

  public List<SimpleColumnCombination> createInitialCombinations(
      ColumnStore[] stores) throws InputGenerationException {
    List<SimpleColumnCombination> combinations = new ArrayList<>();
    for (int i = 0; i < stores.length; i++) {
      int columns = stores[i].getNumberOfColumns();

      for (int j = 0; j < columns; j++) {
        if (ignoreAllConstantColumns && stores[i].getIsConstantColumn()[j]) {
          continue;
        }
        if (ignoreNullValueColumns && stores[i].getIsConstantColumn()[j] //Todo: why necessary!!! second line should be enough!!
            && stores[i].getConstantColumnValues()[j] == ColumnStore.NULLHASH) {
          continue;
        }

        combinations.add(SimpleColumnCombination.create(i, j));
      }
    }
    return combinations;
  }


  private List<SimpleColumnCombination> getCombinations(List<SimpleInd> candidates) {
    Set<SimpleColumnCombination> combinations = new HashSet<>();
    for (SimpleInd candidate : candidates) {
      combinations.add(candidate.left);
      combinations.add(candidate.right);
    }
    return new ArrayList<>(combinations);
  }

  public long getAvailableMemory() {
    int mb = 1024 * 1024;
    Runtime runtime = Runtime.getRuntime();
    return (runtime.totalMemory() - runtime.freeMemory()) / mb;
  }

  private void close(RelationalInput input) {
    try {
      input.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
