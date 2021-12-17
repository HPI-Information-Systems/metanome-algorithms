package de.metanome.algorithms.tireless.preprocessing;

import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InputReaderTest {

    public RelationalInput getTestInput() {
        return new RelationalInput() {
            private int count = 0;
            private final List<List<String>> values = new ArrayList<>() {{
                add(new ArrayList<>() {{
                    add("abc");
                    add("123");
                }});
                add(new ArrayList<>() {{
                    add("abc");
                    add("456");
                }});
                add(new ArrayList<>() {{
                    add("def");
                    add("789");
                }});
            }};

            @Override
            public boolean hasNext() throws InputIterationException {
                return count < values.size();
            }

            @Override
            public List<String> next() throws InputIterationException {
                return values.get(count++);
            }

            @Override
            public int numberOfColumns() {
                return 2;
            }

            @Override
            public String relationName() {
                return null;
            }

            @Override
            public List<String> columnNames() {
                return null;
            }

            @Override
            public void close() throws Exception {

            }
        };
    }

    @Test
    public void testTransposedInput() throws InputIterationException {
        RelationalInput input = getTestInput();
        InputReader reader = new InputReader(input);

        List<Map<String, Integer>> processed = reader.getValues();

        assert(processed.get(0).containsKey("abc"));
        assert(processed.get(0).containsKey("def"));

        assert(processed.get(1).containsKey("123"));
        assert(processed.get(1).containsKey("456"));
        assert(processed.get(1).containsKey("789"));
    }

    @Test
    public void testDistinctValues() throws InputIterationException {
        RelationalInput input = getTestInput();
        InputReader reader = new InputReader(input);

        List<Map<String, Integer>> processed = reader.getValues();

        assert(processed.get(0).size() == 2);
        assert(processed.get(1).size() == 3);
    }

    @Test
    public void testCount() throws InputIterationException {
        RelationalInput input = getTestInput();
        InputReader reader = new InputReader(input);

        List<Map<String, Integer>> processed = reader.getValues();

        assert(processed.get(0).get("abc") == 2);
        assert(processed.get(0).get("def") == 1);

        assert(processed.get(1).get("123") == 1);
        assert(processed.get(1).get("456") == 1);
        assert(processed.get(1).get("789") == 1);
    }
}
