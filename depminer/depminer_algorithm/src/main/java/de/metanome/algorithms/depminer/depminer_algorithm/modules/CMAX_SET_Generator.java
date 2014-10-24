package de.metanome.algorithms.depminer.depminer_algorithm.modules;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithms.depminer.depminer_helper.modules.Algorithm_Group2_Modul;
import de.metanome.algorithms.depminer.depminer_helper.modules.container.AgreeSet;
import de.metanome.algorithms.depminer.depminer_helper.modules.container.CMAX_SET;
import de.metanome.algorithms.depminer.depminer_helper.modules.container.MAX_SET;
import org.apache.lucene.util.OpenBitSet;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public class CMAX_SET_Generator extends Algorithm_Group2_Modul {

    private List<MAX_SET> maxSet;
    private List<CMAX_SET> cmaxSet;

    private List<AgreeSet> agreeSets;
    private int numberOfAttributes;

    public CMAX_SET_Generator(int numberOfThreads, List<AgreeSet> agreeSets, int numberOfAttributes) {

        super(numberOfThreads, "CMAX_SET_Gen");

        this.agreeSets = agreeSets;
        this.numberOfAttributes = numberOfAttributes;

    }

    public List<CMAX_SET> execute() throws AlgorithmExecutionException {

        if (this.timeMesurement) {
            this.startTime();
        }

        this.generateMaxSet();

        this.generateCMAX_SETs();

        if (this.timeMesurement) {
            this.stopTime();
        }

        return this.cmaxSet;

    }

    private void generateCMAX_SETs() throws AlgorithmExecutionException {

        if (this.optimize()) {
            this.cmaxSet = new CopyOnWriteArrayList<CMAX_SET>();
            ExecutorService exec = this.getExecuter();
            for (int i = 0; i < this.numberOfAttributes; ++i) {
                exec.execute(new CMAX_SET_JOB(i));
            }
            this.awaitExecuter(exec);
        } else {
            this.cmaxSet = new LinkedList<CMAX_SET>();
            for (int i = 0; i < this.numberOfAttributes; ++i) {
                executeCMAX_SET_Task(i);
            }

        }

    }

    private void generateMaxSet() throws AlgorithmExecutionException {

        if (this.optimize()) {
            this.maxSet = new CopyOnWriteArrayList<MAX_SET>();
            ExecutorService exec = this.getExecuter();
            for (int i = 0; i < this.numberOfAttributes; ++i) {
                exec.execute(new RunnerThreadMaxSet(i));
            }
            this.awaitExecuter(exec);
        } else {
            this.maxSet = new LinkedList<MAX_SET>();
            for (int i = 0; i < this.numberOfAttributes; ++i) {
                executeMax_Set_Task(i);
            }
        }

    }

    private void executeMax_Set_Task(int currentJob) {

        MAX_SET result = new MAX_SET(currentJob);
        for (AgreeSet a : this.agreeSets) {
            OpenBitSet content = a.getAttributes();
            if (content.get(currentJob)) {
                continue;
            }
            result.addCombination(content);
        }
        result.finalize();
        this.maxSet.add(result);
    }

    private void executeCMAX_SET_Task(int currentJob) {

        MAX_SET maxSet = null;
        for (MAX_SET m : this.maxSet) {
            if (m.getAttribute() == currentJob) {
                maxSet = m;
                break;
            }
        }

        CMAX_SET result = new CMAX_SET(currentJob);

        for (OpenBitSet il : maxSet.getCombinations()) {
            OpenBitSet inverse = new OpenBitSet();
            inverse.set(0, this.numberOfAttributes);
            inverse.xor(il);
            result.addCombination(inverse);
        }

        result.finalize();
        this.cmaxSet.add(result);
    }

    private class RunnerThreadMaxSet implements Runnable {

        private int task;

        public RunnerThreadMaxSet(int task) {

            this.task = task;
        }

        @Override
        public void run() {

            executeMax_Set_Task(this.task);

        }

    }

    private class CMAX_SET_JOB implements Runnable {

        private int job;

        public CMAX_SET_JOB(int job) {

            this.job = job;
        }

        @Override
        public void run() {

            executeCMAX_SET_Task(this.job);

        }

    }

}
