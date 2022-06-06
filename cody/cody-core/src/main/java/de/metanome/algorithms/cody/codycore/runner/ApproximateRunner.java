package de.metanome.algorithms.cody.codycore.runner;

import ch.javasoft.bitset.search.TreeSearch;
import de.metanome.algorithms.cody.codycore.Configuration;
import de.metanome.algorithms.cody.codycore.Preprocessor;
import de.metanome.algorithms.cody.codycore.Validator;
import de.metanome.algorithms.cody.codycore.candidate.CheckedColumnCombination;
import de.metanome.algorithms.cody.codycore.candidate.ColumnCombination;
import de.metanome.algorithms.cody.codycore.candidate.ColumnCombinationUtils;
import de.metanome.algorithms.cody.codycore.pruning.ComponentPruner;
import de.metanome.algorithms.cody.codycore.pruning.PrunerFactory;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Multimap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ApproximateRunner extends BaseRunner {

    public ApproximateRunner(@NonNull Configuration configuration) {
        super(configuration);
    }

    /**
     * Run the approximate Cody algorithm with the set configuration
     * When finished, the results can be retrieved with getResultSet
     */
    @Override
    public void run() {
        Stopwatch completeWatch = Stopwatch.createStarted();
        log.info("Start running approximate Cody algorithm with configuration: {}", this.configuration);

        Stopwatch prepareWatch = Stopwatch.createStarted();
        Preprocessor preprocessor = new Preprocessor(this.configuration);
        preprocessor.run();
        log.info("Preprocessing took: {} ms", prepareWatch.stop().elapsed(TimeUnit.MILLISECONDS));

        Stopwatch validatorWatch = Stopwatch.createStarted();
        Validator validator = new Validator(this.configuration, preprocessor.getColumnPlis(),
                preprocessor.getNRows(), preprocessor.getRowCounts());
        log.info("Unary candidate validation took: {} ms", validatorWatch.stop().elapsed(TimeUnit.MILLISECONDS));

        Stopwatch prunerWatch = Stopwatch.createStarted();
        ComponentPruner pruner = PrunerFactory.create(this.configuration, validator.getGraphView());
        pruner.run();

        Multimap<Integer, ColumnCombination> optimisticCandidates = pruner.getResultSet();
        int maxCardinality = optimisticCandidates.isEmpty() ? 0 : Collections.max(optimisticCandidates.keySet());
        log.info("Estimating upper bounds took: {} ms", prunerWatch.stop().elapsed(TimeUnit.MILLISECONDS));

        Stopwatch latticeTraversal = Stopwatch.createStarted();
        List<ColumnCombination> currentLevelCandidates = new ArrayList<>();
        TreeSearch maximalValidColumnCombinations = new TreeSearch();
        for (int level = maxCardinality; level >= 2; level--) {
            currentLevelCandidates.addAll(optimisticCandidates.get(level));

            log.info("At level: {} with: {} candidates", level, currentLevelCandidates.size());
            List<CheckedColumnCombination> checkedCurrentLevelCandidates = currentLevelCandidates
                    .parallelStream()
                    .map(validator::checkColumnCombination)
                    .collect(Collectors.toList());

            checkedCurrentLevelCandidates
                    .stream()
                    .filter(c -> c.getSupport() >= this.configuration.getMinSupport())
                    .forEach(c -> {
                        maximalValidColumnCombinations.add(c.getColumns());
                        this.resultSet.add(c);
                        log.debug("Found valid candidate: {}", c);
                    });

            currentLevelCandidates = checkedCurrentLevelCandidates
                    .parallelStream()
                    .filter(c -> c.getSupport() < this.configuration.getMinSupport())
                    .flatMap(c -> ColumnCombinationUtils.getImmediateSubsets(c).stream())
                    .distinct()
                    .filter(c -> maximalValidColumnCombinations.findSuperSet(c.getColumns()) == null)
                    .collect(Collectors.toList());
        }
        log.info("Candidate validation took: {} ms", latticeTraversal.stop().elapsed(TimeUnit.MILLISECONDS));

        Stopwatch postProcessingWatch = Stopwatch.createStarted();
        this.resultSet = this.resultSet.stream().map(c -> ColumnCombinationUtils.inflateDuplicateColumns(c,
                preprocessor.getColumnIndexToDuplicatesMapping())).collect(Collectors.toList());
        log.info("Candidate post-processing took: {} ms", postProcessingWatch.stop().elapsed(TimeUnit.MILLISECONDS));

        log.info("Complete approximate Cody algorithm took: {} ms",
                completeWatch.stop().elapsed(TimeUnit.MILLISECONDS));

        log.info("ResultSet with {} Codys:", this.resultSet.size());
        for (CheckedColumnCombination c : this.resultSet)
            log.info("{}", c.toString(preprocessor.getColumnIndexToNameMapping()));
    }
}
