package de.metanome.algorithms.cfdfinder.result;

import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.result_receiver.ConditionalFunctionalDependencyResultReceiver;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FileResultStrategy extends ResultStrategy {

    private static String resultDir = "results/";
    private String filePath;
    private long startTime;
    private BufferedWriter writer;


    public FileResultStrategy(ConditionalFunctionalDependencyResultReceiver resultReceiver, ObjectArrayList<ColumnIdentifier> columnIdentifiers, String fileName) {
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
        return "FileStrategy";
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
    public void receiveResult(Result result) {
        numResults += 1;
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
