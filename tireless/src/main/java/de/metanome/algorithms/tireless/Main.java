package de.metanome.algorithms.tireless;

import de.metanome.algorithm_integration.AlgorithmExecutionException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, AlgorithmExecutionException {
        TirelessAlgorithmLocal local = new TirelessAlgorithmLocal();
        local.execute();
    }

}
