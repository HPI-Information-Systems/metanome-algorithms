package de.metanome.algorithms.cfdfinder.result;

import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.result_receiver.ConditionalFunctionalDependencyResultReceiver;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PruningLatticeResultStrategy extends ResultStrategy {

    private List<ResultLattice> resultLattices;

    public PruningLatticeResultStrategy(ConditionalFunctionalDependencyResultReceiver resultReceiver, ObjectArrayList<ColumnIdentifier> columnIdentifiers) {
        super(resultReceiver, columnIdentifiers);
    }

    public static String getIdentifier() {
        return "SupportDropPruning";
    }

    @Override
    public void startReceiving() {
        super.startReceiving();
        resultLattices = new LinkedList<>();
    }

    @Override
    protected void sendToMetanome(Result result) {
        super.sendToMetanome(result);
    }

    @Override
    public void stopReceiving() {
        super.stopReceiving();
        Set<Result> results = new HashSet<>();
        for (ResultLattice resultTree : resultLattices) {
            results.addAll(resultTree.getLeaves());
        }
        for (Result result : results) {
            sendToMetanome(result);
        }
    }

    @Override
    public void receiveResult(Result result) {
        boolean inserted = false;
        for (ResultLattice tree : resultLattices) {
            inserted = inserted || tree.insert(result);
        }
        if (!inserted) {
            resultLattices.add(new ResultLattice(result));
        }
    }
}
