package de.metanome.algorithms.fastfds;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithms.fastfds.fastfds_helper.modules.AgreeSetGenerator;
import de.metanome.algorithms.fastfds.fastfds_helper.modules.StrippedPartitionGenerator;
import de.metanome.algorithms.fastfds.fastfds_helper.modules.container.AgreeSet;
import de.metanome.algorithms.fastfds.fastfds_helper.modules.container.StrippedPartition;
import de.metanome.algorithms.fastfds.modules.DifferenceSetGenerator;
import de.metanome.algorithms.fastfds.modules.FindCoversGenerator;
import de.metanome.algorithms.fastfds.modules.container.DifferenceSet;

import java.util.List;

public class FastFD {

    private int numberOfThreads;
    private FunctionalDependencyResultReceiver fdrr;

    public FastFD(int numberOfThreads, FunctionalDependencyResultReceiver resultReceiver) {

        this.numberOfThreads = numberOfThreads;
        this.fdrr = resultReceiver;

    }

    public void execute(RelationalInput input) throws AlgorithmExecutionException {

        List<StrippedPartition> strippedPartitions = new StrippedPartitionGenerator(this.numberOfThreads).execute(input);

        List<AgreeSet> agreeSets = new AgreeSetGenerator(this.numberOfThreads).execute(strippedPartitions);
        // FIXME --> Tommy :D
        agreeSets.add(new AgreeSet());

        List<DifferenceSet> diffSets = new DifferenceSetGenerator(this.numberOfThreads, agreeSets, input.numberOfColumns())
                .execute();
        new FindCoversGenerator(this.fdrr, input.columnNames(), input.relationName(), this.numberOfThreads).execute(diffSets,
                input.numberOfColumns());

    }

}
