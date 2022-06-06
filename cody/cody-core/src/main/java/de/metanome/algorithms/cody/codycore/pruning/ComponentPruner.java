package de.metanome.algorithms.cody.codycore.pruning;

import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.cody.codycore.Configuration;
import de.metanome.algorithms.cody.codycore.candidate.ColumnCombination;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class ComponentPruner {

    protected final List<List<Double>> graph;
    protected final Configuration configuration;
    protected final int[] vertexColouring;
    protected final LongBitSet visited;
    protected final List<List<Integer>> intermediateResultSet;

    /**
     * Mapping of cardinality to the list of found ColumnCombinations with that cardinality
     */
    @Getter protected final Multimap<Integer, ColumnCombination> resultSet;

    public ComponentPruner(@NonNull List<List<Double>> graph, @NonNull Configuration configuration) {
        this.graph = graph;
        this.configuration = configuration;
        this.intermediateResultSet = new ArrayList<>();
        this.vertexColouring = new int[graph.size()];
        this.visited = new LongBitSet();
        this.resultSet = MultimapBuilder.treeKeys().arrayListValues().build();
    }

    /**
     * Run the algorithm with the respective configuration and graph
     * When finished, the result is available with getResultSet
     */
    public void run() {
        this.searchComponents();
        log.info("Found {} components while colouring vertices", this.intermediateResultSet.size());
        this.buildCandidates();
        log.info("Found {} optimistic candidates", this.resultSet.size());
    }

    /**
     * Implementation of a BFS with a two colouring of the bipartite components
     * (Subgraphs are expected to be bipartite; no validation)
     */
    protected void searchComponents() {
        // run a DFS for each unvisited vertex
        for (int i = 0; i < this.graph.size(); i++) {
            if (!this.visited.get(i)) {
                List<Integer> aggregator = new ArrayList<>();
                this.intermediateResultSet.add(aggregator);
                this.searchComponentsCallRec(aggregator, i, 1);
            }
        }
    }

    protected void searchComponentsCallRec(final List<Integer> aggregator, final int vertex, final int colouring) {
        if (this.vertexColouring[vertex] != 0) throw new InternalError("Vertex has already been visited");

        this.vertexColouring[vertex] = colouring;
        this.visited.set(vertex);
        aggregator.add(vertex);

        for (int neighbour : this.getComplementNeighbours(vertex)) {
            if (!this.visited.get(neighbour)) {
                this.searchComponentsCallRec(aggregator, neighbour, colouring + 1);
            }
        }
    }

    protected List<Integer> getComplementNeighbours(final int vertex) {
        return IntStream
                .range(0, this.graph.size())
                .filter(i -> i != vertex)
                .filter(i -> this.graph.get(vertex).get(i) >= this.configuration.getMinSupport())
                .boxed()
                .collect(Collectors.toList());
    }

    protected void buildCandidates() {
        // build ColumnCombinations from DFS search results
        for (List<Integer> component : this.intermediateResultSet) {
            if (component.size() == 1) continue;

            LongBitSet left = new LongBitSet();
            LongBitSet right = new LongBitSet();
            for (int i : component) {
                if ((this.vertexColouring[i] % 2) == 0) {
                    left.set(i);
                } else {
                    right.set(i);
                }
            }

            ColumnCombination newCandidate = new ColumnCombination(left, right);
            this.resultSet.put(newCandidate.getColumns().cardinality(), newCandidate);
        }
    }
}
