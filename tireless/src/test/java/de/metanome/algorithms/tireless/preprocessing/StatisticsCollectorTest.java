package de.metanome.algorithms.tireless.preprocessing;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class StatisticsCollectorTest {

    @Test
    public void testSomeExcludedChars() {
        Map<String, Integer> values = new HashMap<>() {{
            put("abe", 1);
            put("ade", 1);
            put("adf", 8);
        }};

        BitSet specialCharacters = new BitSet() {{
            set('a');
            set('d');
            set('e');
        }};

        StatisticsCollector statisticsCollector = new StatisticsCollector(values, specialCharacters,
                getConfiguration(0.9));


        BitSet expected = new BitSet() {{
            set('e');
        }};

        assertEquals(expected, statisticsCollector.getExcludedSpecials());
    }

    @Test
    public void testAllExcludedChars() {
        Map<String, Integer> values = new HashMap<>() {{
            put("abc", 1);
            put("def", 1);
        }};

        BitSet specialCharacters = new BitSet() {{
            set('a', 'f' + 1);
        }};

        StatisticsCollector statisticsCollector = new StatisticsCollector(values, specialCharacters,
                getConfiguration(0.6));

        assertEquals(specialCharacters, statisticsCollector.getExcludedSpecials());
    }

    @Test
    public void testNoneExcludedChars() {
        Map<String, Integer> values = new HashMap<>() {{
            put("abc", 1);
            put("def", 1);
        }};

        BitSet specialCharacters = new BitSet() {{
            set('a', 'f' + 1);
        }};

        StatisticsCollector statisticsCollector = new StatisticsCollector(values, specialCharacters,
                getConfiguration(0.1));

        BitSet expected = new BitSet();

        assertEquals(expected, statisticsCollector.getExcludedSpecials());
    }

    private AlgorithmConfiguration getConfiguration(double configValue) {
        return new AlgorithmConfiguration(0, configValue,
                0, 0, 0,
                0, 0);
    }
}
