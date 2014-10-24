package de.metanome.algorithms.depminer.depminer_helper.testRunner;

public class MemorySniffingThread extends Thread {

    public long memPeak = 0;
    private boolean stop = false;
    private long startValue;


    public MemorySniffingThread() {

        Runtime rt = Runtime.getRuntime();
        this.startValue = rt.totalMemory() - rt.freeMemory();
    }


    @Override
    public void run() {

        while (!this.stop) {

            Runtime rt = Runtime.getRuntime();
            long newValue = rt.totalMemory() - rt.freeMemory() - this.startValue;
            if (this.memPeak < newValue) {
                this.memPeak = newValue;
            }

            try {
                Thread.sleep(100l);
            } catch (InterruptedException e) {
            }

        }

    }

    public void stopThis() {

        this.stop = true;
    }

}
