package de.metanome.algorithms.anelosimus.driver;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.results.InclusionDependency;

public class SynchronizedDiscInclusionDependencyResultReceiver implements InclusionDependencyResultReceiver {

    PrintWriter writer;

    public SynchronizedDiscInclusionDependencyResultReceiver(String fileName) {
        try {
            writer = new PrintWriter(fileName, "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        writer.close();
    }

    @Override
    public synchronized void receiveResult(InclusionDependency ind) throws CouldNotReceiveResultException {
        final StringBuilder builder = new StringBuilder();
        /*
         * builder.append(ind .getDependant() .getColumnIdentifiers() .iterator() .next() .toString() .substring(0,
         * ind.getDependant().getColumnIdentifiers().iterator().next().toString().indexOf("."))); builder.append(",");
         * builder.append(ind .getReferenced() .getColumnIdentifiers() .iterator() .next() .toString() .substring(0,
         * ind.getReferenced().getColumnIdentifiers().iterator().next().toString().indexOf(".")));
         * builder.append("\r\n");
         */
        builder.append(ind.toString());
        builder.append("\n");
        writer.write(builder.toString());
    }

}
