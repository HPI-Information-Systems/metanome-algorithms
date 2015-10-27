package de.hpi.mpss2015n.approxind.utils;


public class DebugCounter {
    public static final int COUNT_PER_DOT = 1_000_000;
    public static final int DOT_PER_NUMBER = 10;
    public static final int NUMBER_PER_LINE = 5;

    private long rowsWritten = 0;

    public DebugCounter() {
    }

    /**
     * print to console every 1mio calls.
     */
    public void countUp() {
        rowsWritten++;
        if(rowsWritten % COUNT_PER_DOT == 0) {
            if(rowsWritten % (COUNT_PER_DOT * DOT_PER_NUMBER) == 0) {
                System.out.print(rowsWritten/1_000_000 + "m");
                if(rowsWritten % (COUNT_PER_DOT * DOT_PER_NUMBER * NUMBER_PER_LINE) == 0) {
                    System.out.println();
                }
            } else {
                System.out.print('.');
            }
        }
    }

    public void done() {
        System.out.println();
    }
}