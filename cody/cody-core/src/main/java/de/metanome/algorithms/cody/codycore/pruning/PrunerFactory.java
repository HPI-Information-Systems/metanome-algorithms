package de.metanome.algorithms.cody.codycore.pruning;

import de.metanome.algorithms.cody.codycore.Configuration;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class PrunerFactory {

    /**
     * Creates a ComponentPruner for exact and a CliquePruner for approximate Cody discovery
     *
     * @param configuration's minSupport property is used to determine best Pruner
     * @param graph with which the Pruner will be initialized
     * @return the optimal Pruner
     */
    public ComponentPruner create(@NonNull Configuration configuration, @NonNull List<List<Double>> graph) {
        if (configuration.isNoCliqueSearch() || configuration.getMinSupport() == 1.0) {
            return new ComponentPruner(graph, configuration);
        } else {
            return new CliquePruner(graph, configuration);
        }
    }
}
