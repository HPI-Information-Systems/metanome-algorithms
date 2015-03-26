package de.metanome.algorithms.anelosimus.test;

import java.util.Arrays;
import java.util.List;

import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;

public class RelationalInputMock implements RelationalInput {

    String name;
    String[] header;
    String[][] data;
    int row = 0;

    public RelationalInputMock(String name, String[] header, String[][] data) {
        this.name = name;
        this.header = header;
        this.data = data;
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public boolean hasNext() throws InputIterationException {
        return row < data.length;
    }

    @Override
    public List<String> next() throws InputIterationException {
        return Arrays.asList(data[row++]);
    }

    @Override
    public int numberOfColumns() {
        return header.length;
    }

    @Override
    public String relationName() {
        return this.name;
    }

    @Override
    public List<String> columnNames() {
        return Arrays.asList(header);
    }

    public void reset() {
        this.row = 0;
    }

}
