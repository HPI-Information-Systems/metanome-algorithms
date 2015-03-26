package de.metanome.algorithms.anelosimus.io;

import java.util.ArrayList;
import java.util.List;

import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;

public class FileInputIterator implements InputIterator {

    private RelationalInput input = null;
    private List<String> record = null;
    private int rowsRead = 0;
    private int inputRowLimit;

    public FileInputIterator(RelationalInput input, int inputRowLimit)
            throws InputGenerationException {
        this.input = input;
        this.inputRowLimit = inputRowLimit;
    }

    @Override
    public boolean next() throws InputIterationException {
        if (this.input.hasNext() && ((this.inputRowLimit <= 0) || (this.rowsRead < this.inputRowLimit))) {
            this.record = this.input.next();
            this.rowsRead++;
            return true;
        }
        return false;
    }

    @Override
    public String getValue(int columnIndex) {
        String value = this.record.get(columnIndex);
        return ("".equals(value)) ? null : value;
    }

    @Override
    public List<String> getValues(int numColumns) {
        List<String> values = new ArrayList<String>(numColumns);
        for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
            String value = this.record.get(columnIndex);
            if ("".equals(value))
                values.add(null);
            else
                values.add(value);
        }
        return values;
    }

    @Override
    public void close() throws Exception {
        this.input.close();
    }
}
