package de.metanome.algorithms.depminer.depminer_algorithm.modules;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithms.depminer.depminer_helper.modules.Algorithm_Group2_Modul;
import de.metanome.algorithms.depminer.depminer_helper.modules.container.FunctionalDependencyGroup2;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class FunctionalDependencyGenerator extends Algorithm_Group2_Modul {

    private FunctionalDependencyResultReceiver fdrr;
    private String relationName;
    private List<String> columns;
    private Int2ObjectMap<List<BitSet>> lhss;

    private AlgorithmExecutionException exception = null;

    private List<FunctionalDependencyGroup2> result;

    public FunctionalDependencyGenerator(FunctionalDependencyResultReceiver fdrr, String relationName,
                                         List<String> columnIdentifer, int numberOfThreads, Int2ObjectMap<List<BitSet>> lhss) {

        super(numberOfThreads, "FunctionalDependencyGen");

        this.fdrr = fdrr;
        this.relationName = relationName;
        this.columns = columnIdentifer;
        this.lhss = lhss;
    }

    public List<FunctionalDependencyGroup2> execute() throws AlgorithmExecutionException {

        if (this.timeMesurement) {
            this.startTime();
        }

        if (this.optimize()) {

            this.result = new CopyOnWriteArrayList<FunctionalDependencyGroup2>();
            ExecutorService exec = this.getExecuter();
            for (int attribute : this.lhss.keySet()) {
                exec.execute(new ParallelTask(attribute));
            }
            this.awaitExecuter(exec);

        } else {

            this.result = new LinkedList<FunctionalDependencyGroup2>();
            for (int attribute : this.lhss.keySet()) {
                executePara(attribute);
            }

        }

        if (this.exception != null) {
            throw this.exception;
        }

        if (this.timeMesurement) {
            this.stopTime();
        }

        return this.result;
    }

    // TODO: find better name for method
    private void executePara(int attribute) throws CouldNotReceiveResultException, ColumnNameMismatchException {

        for (BitSet lhs : this.lhss.get(attribute)) {
            if (lhs.get(attribute)) {
                continue;
            }
            IntList bits = new IntArrayList();
            int lastIndex = lhs.nextSetBit(0);
            while (lastIndex != -1) {
                bits.add(lastIndex);
                lastIndex = lhs.nextSetBit(lastIndex + 1);
            }

            FunctionalDependencyGroup2 fdg = new FunctionalDependencyGroup2(attribute, bits);
            this.fdrr.receiveResult((fdg.buildDependency(this.relationName, this.columns)));
            this.result.add(fdg);
        }
    }

    private class ParallelTask implements Runnable {

        private int task;

        public ParallelTask(int task) {

            this.task = task;
        }

        @Override
        public void run() {

            try {
                executePara(this.task);
            } catch (CouldNotReceiveResultException e) {
                exception = e;
            } catch (ColumnNameMismatchException e) {
            	exception = e;
			}

        }

    }

}
