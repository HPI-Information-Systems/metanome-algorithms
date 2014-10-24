package de.metanome.algorithms.depminer.depminer_helper.testRunner;


import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithms.depminer.depminer_helper.AlgorithmMetaGroup2;

public class AlgorithExecuteThread extends Thread {

    public long time;
    public Exception e = null;
    private AlgorithmMetaGroup2 algo;
    private CSVTestCase csvt;

    public AlgorithExecuteThread(AlgorithmMetaGroup2 algo, CSVTestCase csvt) {

        this.algo = algo;
        this.csvt = csvt;
    }

    @Override
    public void run() {

        try {
            algo.setRelationalInputConfigurationValue(AlgorithmMetaGroup2.INPUT_TAG, csvt);
            algo.setBooleanConfigurationValue(AlgorithmMetaGroup2.USE_OPTIMIZATIONS_TAG, false);
            algo.setResultReceiver(csvt);

            time = System.currentTimeMillis();
            algo.execute();
            time = System.currentTimeMillis() - time;
        } catch (AlgorithmConfigurationException e) {
            this.e = e;
        } catch (AlgorithmExecutionException e) {
            this.e = e;
        }

    }

}
