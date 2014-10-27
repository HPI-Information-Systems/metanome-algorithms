package de.metanome.algorithms.fastfds.TestRunner;

import de.metanome.algorithms.fastfds.AlgorithmGroup2FastFD;
import de.metanome.algorithms.fastfds.fastfds_helper.testRunner.AlgorithExecuteThread;
import de.metanome.algorithms.fastfds.fastfds_helper.testRunner.CSVTestCase;
import de.metanome.algorithms.fastfds.fastfds_helper.testRunner.MemorySniffingThread;

import java.io.IOException;

public class RunnerFastFD {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws IOException {

        CSVTestCase.init();

        for (String fileName : CSVTestCase.getAllFileNames()) {

            CSVTestCase.writeToResultFile("###Starte: " + fileName);

            String content = "";

            CSVTestCase csvt = null;
            try {
                csvt = new CSVTestCase(fileName, !(fileName.contains("error") || fileName.contains("request")));
            } catch (IOException e) {
                content = fileName + ";" + "Fehler bei TestCase-Erstellung";
            }

            if (csvt != null) {

                MemorySniffingThread mst = new MemorySniffingThread();
                mst.start();

                AlgorithExecuteThread aet = new AlgorithExecuteThread(new AlgorithmGroup2FastFD(), csvt);
                aet.start();

                while (aet.isAlive()) {

                    try {
                        Thread.sleep(10000l);
                    } catch (InterruptedException e) {
                    }

//					Runtime.getRuntime().gc();
                    long availableMem = Runtime.getRuntime().freeMemory();

                    // 100mb
                    if (availableMem < 105000000) {
                        System.out.println(availableMem / 1024 / 1024);
                        content = fileName + ";" + "MemoryOverflow";
                        aet.stop();
                    }

                }

                if (aet.e != null) {
                    content = fileName + ";" + "Fehler bei der Ausführung des Testcases";
                }

                mst.stopThis();
                try {
                    mst.join();
                } catch (InterruptedException e) {
                    content = fileName + ";" + "MemoryMessung unvollständig";
                }

                if (content.equals("")) {
                    content = fileName + ";" + aet.time + ";" + mst.memPeak;
                }
            }

            CSVTestCase.writeToResultFile(content);
            System.out.println("Finished: " + fileName);

            csvt.close();
        }


    }
}
