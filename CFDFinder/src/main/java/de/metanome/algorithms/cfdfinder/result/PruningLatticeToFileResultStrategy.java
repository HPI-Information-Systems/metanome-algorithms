package de.metanome.algorithms.cfdfinder.result;

import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.result_receiver.ConditionalFunctionalDependencyResultReceiver;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PruningLatticeToFileResultStrategy extends PruningLatticeResultStrategy {

    private static String resultDir = "results/";
    private String filePath;
    private long startTime;
    private BufferedWriter writer;

    private PruningLatticeToFileResultStrategy(ConditionalFunctionalDependencyResultReceiver resultReceiver, ObjectArrayList<ColumnIdentifier> columnIdentifiers) {
        super(resultReceiver, columnIdentifiers);
    }

    public PruningLatticeToFileResultStrategy(ConditionalFunctionalDependencyResultReceiver resultReceiver, ObjectArrayList<ColumnIdentifier> columnIdentifiers, String fileName) {
        super(resultReceiver, columnIdentifiers);
        if (fileName != null) {
            this.filePath = resultDir + fileName;
        } else {
            this.filePath = resultDir + String.valueOf(System.nanoTime()) + ".txt";
        }
        try {
            writer = new BufferedWriter(new FileWriter(filePath, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getIdentifier() {
        return "PruningLatticeFileStrategy";
    }

    @Override
    public void startReceiving() {
        super.startReceiving();
        startTime = System.nanoTime();
    }

    @Override
    public void stopReceiving() {
        super.stopReceiving();
        append(String.valueOf(TimeUnit.NANOSECONDS.toSeconds((System.nanoTime() - startTime) * 1000) / 1000f) + " seconds");
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void sendToMetanome(Result result) {
        super.sendToMetanome(result);
        append(result.toString());
        append("\n\n");
    }

    private void append(String s) {
        try {
            writer.append(s);
            writer.flush();
        } catch (IOException e) {
            System.out.println("Could not write to file " + filePath);
            e.printStackTrace();
        }
    }


}
