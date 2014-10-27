package de.metanome.algorithms.fastfds.fastfds_helper.modules;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithms.fastfds.fastfds_helper.testRunner.CSVTestCase;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class Algorithm_Group2_Modul {

    protected boolean timeMesurement = false;
    protected boolean debugSysout = false;

    protected int numberOfThreads;
    protected String nameOfModule;

    protected long time;

    protected Algorithm_Group2_Modul(int numberOfThreads, String nameOfModule) {

        this.numberOfThreads = numberOfThreads;
        this.nameOfModule = nameOfModule;
    }

    protected ExecutorService getExecuter() {

        return Executors.newFixedThreadPool(this.numberOfThreads);

    }

    protected boolean optimize() {

        return this.numberOfThreads > 1;
    }

    protected void awaitExecuter(ExecutorService exec) throws AlgorithmExecutionException {

        exec.shutdown();

        try {
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new AlgorithmExecutionException("Ausführung wurde in " + this.nameOfModule + " interrupted: " + e.getMessage());
        }
    }

    protected void startTime() {

        this.time = System.currentTimeMillis();
    }

    protected void stopTime() {

        try {
            CSVTestCase.writeToResultFile(this.nameOfModule + ";" + (System.currentTimeMillis() - this.time));
        } catch (IOException e) {
        }
        // System.out.println("Dauer von " + this.nameOfModule + ": " + (System.currentTimeMillis() - this.time));
    }

}
