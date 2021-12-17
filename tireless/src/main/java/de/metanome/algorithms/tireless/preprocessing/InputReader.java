package de.metanome.algorithms.tireless.preprocessing;

import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;

import java.util.*;

public class InputReader {

    private final RelationalInput input;
    private List<Map<String, Integer>> values;

    public InputReader(RelationalInput input) {
        this.input = input;
        this.values = null;
    }

    public List<Map<String, Integer>> getValues() throws InputIterationException {
        final int columnCount = input.numberOfColumns();
        initializeResultStructure(columnCount);
        while(input.hasNext()) {
            List<String> record = input.next();
            for(int i = 0; i < columnCount; i++)
                values.get(i).put(record.get(i), values.get(i).getOrDefault(record.get(i), 0) + 1);
        }
        return values;
    }

    private void initializeResultStructure(int columnCount) {
        List<Map<String, Integer>> result = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            result.add(new HashMap<>());
        }
        this.values = result;
    }
}
