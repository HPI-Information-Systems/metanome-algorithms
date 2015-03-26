package de.metanome.algorithms.anelosimus.helper;

import java.util.List;

import de.metanome.algorithms.anelosimus.bitvectors.BitVector;

public class PrintHelper {

    public static String printMatrix(List<BitVector<?>> matrix) {
        StringBuffer matrixString = new StringBuffer();

        for (BitVector<?> row : matrix) {
            matrixString.append(row.toString() + "\n");
        }
        return matrixString.toString();
    }
}
