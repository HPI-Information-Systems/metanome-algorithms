package de.metanome.algorithms.tireless.preprocessing;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsCollector {

    private final Map<String, Integer> rawData;
    private final BitSet characters;
    private final AlgorithmConfiguration configuration;

    public StatisticsCollector(Map<String, Integer> rawData, BitSet characters, AlgorithmConfiguration configuration) {
        this.rawData = rawData;
        this.characters = characters;
        this.configuration = configuration;
    }

    private Map<Character, Integer> computeCharacterOccurrences() {
        Map<Character, Integer> characterStatistics = new HashMap<>();
        for (int i = characters.nextSetBit(0); i >= 0; i = characters.nextSetBit(i + 1)) {
            int count = 0;
            for (String value : rawData.keySet()) if (value != null && value.indexOf(i) >= 0) count+=rawData.get(value);
            characterStatistics.put((char) i, count);
        }
        return characterStatistics;
    }

    public BitSet getExcludedSpecials() {
        Map<Character, Integer> occurrences = computeCharacterOccurrences();
        double minOccurrences = configuration.MINIMAL_SPECIAL_CHARACTER_OCCURRENCE
                * rawData.values().stream().reduce(0, Integer::sum);
        BitSet excluded = new BitSet();
        for (char character : occurrences.keySet())
            if (occurrences.get(character) < minOccurrences) excluded.set(character);
        return excluded;
    }

}
