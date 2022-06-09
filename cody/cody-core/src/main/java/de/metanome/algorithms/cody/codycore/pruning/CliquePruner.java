package de.metanome.algorithms.cody.codycore.pruning;

import de.metanome.algorithms.cody.codycore.Configuration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class CliquePruner extends ComponentPruner {

    public CliquePruner(@NonNull List<List<Double>> graph, @NonNull Configuration configuration) {
        super(graph, configuration);
    }

    /**
     * Run the algorithm with the respective configuration and graph
     * When finished, the result is available with getResultSet
     */
    @Override
    public void run() {
        this.searchComponents();
        log.info("Found {} components while colouring vertices", this.intermediateResultSet.size());

        // clear resultSet found during vertex coloring
        this.intermediateResultSet.clear();
        this.searchCliques();
        this.buildCandidates();
        log.info("Found {} optimistic candidates", this.resultSet.size());
    }

    /**
     * Implementation of the Bron-Kerbosch clique enumeration algorithm
     *
     * Bron, Coen; Kerbosch, Joep (1973), "Algorithm 457: finding all cliques of an undirected graph"
     * Commun. ACM, ACM, 16 (9): 575–577
     * doi:10.1145/362342.362367.
     *
     */
    private void searchCliques() {
        this.searchCliquesCallRec(this.intermediateResultSet, new ArrayList<>(),
                IntStream.range(0, this.graph.size()).boxed().collect(Collectors.toList()),
                new ArrayList<>());
    }

    private void searchCliquesCallRec(final List<List<Integer>> aggregator, final List<Integer> clique,
                                      final List<Integer> candidates, final List<Integer> excluded) {
        if (candidates.isEmpty() && excluded.isEmpty()) {
            if (this.cliqueHasTwoColours(clique))
                aggregator.add(clique);
            return;
        }

        List<Integer> pivotedCandidates = Lists.newArrayList(candidates);
        for (int n : this.getAllNeighbours(!candidates.isEmpty() ? candidates.get(0) : excluded.get(0)))
            pivotedCandidates.remove(Integer.valueOf(n));

        for (int i : pivotedCandidates) {
            List<Integer> newClique = new ArrayList<>(clique);
            newClique.add(i);

            List<Integer> neighbours = this.getAllNeighbours(i);
            List<Integer> newCandidates = Lists.newArrayList(candidates);
            newCandidates.retainAll(neighbours);

            List<Integer> newExcluded = Lists.newArrayList(excluded);
            newExcluded.retainAll(neighbours);

            this.searchCliquesCallRec(aggregator, newClique, newCandidates, newExcluded);
            candidates.remove(Integer.valueOf(i));
            excluded.add(i);
        }
    }

    private List<Integer> getAllNeighbours(final int vertex) {
        return IntStream
                .range(0, this.graph.size())
                .filter(i -> i != vertex)
                .filter(i -> this.graph.get(vertex).get(i) >= this.configuration.getMinSupport() ||
                        this.graph.get(vertex).get(i) <= 1 - this.configuration.getMinSupport())
                .boxed()
                .collect(Collectors.toList());
    }

    private boolean cliqueHasTwoColours(final List<Integer> clique) {
        return clique.stream().anyMatch(i -> this.vertexColouring[i] % 2 == 0) &&
                clique.stream().anyMatch(j -> this.vertexColouring[j] % 2 != 0);
    }
}
