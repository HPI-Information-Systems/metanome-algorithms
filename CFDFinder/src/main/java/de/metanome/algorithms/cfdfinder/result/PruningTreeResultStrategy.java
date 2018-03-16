package de.metanome.algorithms.cfdfinder.result;

import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.backend.result_receiver.ResultReceiver;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PruningTreeResultStrategy extends ResultStrategy {

    private List<ResultTree> resultTrees;

    public PruningTreeResultStrategy(ResultReceiver resultReceiver, ObjectArrayList<ColumnIdentifier> columnIdentifiers) {
        super(resultReceiver, columnIdentifiers);
    }

    public static String getIdentifier() {
        return "PruningTreeStrategy";
    }

    @Override
    public void startReceiving() {
        super.startReceiving();
        resultTrees = new LinkedList<>();
    }

    @Override
    public void stopReceiving() {
        super.stopReceiving();
        Set<Result> results = new HashSet<>();
        for (ResultTree resultTree : resultTrees) {
            results.addAll(resultTree.getLeaves());
        }
        for (Result result : results) {
            super.sendToMetanome(result);
        }
    }

    @Override
    public void receiveResult(Result result) {
        double minimalDistance = Double.MAX_VALUE;
        ResultTree minimalPosiition = null;
        for (ResultTree tree : resultTrees) {
            ResultTree position = tree.getInsertPosition(result);
            if (position != null) {
                double parentSupport = position.getNode().getPatternTableau().getSupport();
                if (parentSupport - result.getPatternTableau().getSupport() < minimalDistance) {
                    minimalDistance = parentSupport - result.getPatternTableau().getSupport();
                    minimalPosiition = position;
                }
            }
        }
        if (minimalPosiition != null) {
            minimalPosiition.insert(result);
        } else {
            resultTrees.add(new ResultTree(result));
        }
    }
}
