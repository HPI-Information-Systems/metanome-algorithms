package de.metanome.algorithms.fastfds.modules;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithms.fastfds.fastfds_helper.modules.Algorithm_Group2_Modul;
import de.metanome.algorithms.fastfds.fastfds_helper.modules.container.AgreeSet;
import de.metanome.algorithms.fastfds.modules.container.DifferenceSet;

import org.apache.lucene.util.OpenBitSet;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public class DifferenceSetGenerator extends Algorithm_Group2_Modul {

    private int numberOfAttributes;
    private List<AgreeSet> agreeSets;

    private List<DifferenceSet> returnValue;

    public DifferenceSetGenerator(int numberOfThreads, List<AgreeSet> agreeSets, int numberOfAttributes) {

        super(numberOfThreads, "DifferenceSetGen");

        this.agreeSets = agreeSets;
        this.numberOfAttributes = numberOfAttributes;

    }

    public List<DifferenceSet> execute() throws AlgorithmExecutionException {

        if (this.timeMesurement) {
            this.startTime();
        }

        if (this.optimize()) {

            this.returnValue = new CopyOnWriteArrayList<DifferenceSet>();
            ExecutorService exec = this.getExecuter();
            for (AgreeSet as : agreeSets) {
                exec.execute(new DifferenceSetTask(as));
            }
            this.awaitExecuter(exec);

        } else {

            this.returnValue = new LinkedList<DifferenceSet>();
            for (AgreeSet as : agreeSets) {
                this.doTask(as);
            }

        }

        if (this.timeMesurement) {
            this.stopTime();
        }

        return returnValue;
    }

    private void doTask(AgreeSet as) {

        OpenBitSet value = as.getAttributes();

        DifferenceSet ds = new DifferenceSet();
        for (int i = 0; i < numberOfAttributes; i++) {
            if (!value.get(i)) {
                ds.add(i);
            }
        }
        this.returnValue.add(ds);
    }

    private class DifferenceSetTask implements Runnable {

        private AgreeSet task;

        public DifferenceSetTask(AgreeSet as) {

            this.task = as;
        }

        @Override
        public void run() {

            doTask(this.task);

        }

    }
}
