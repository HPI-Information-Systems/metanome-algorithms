package de.metanome.algorithms.cfdfinder.expansion;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.metanome.algorithms.cfdfinder.pattern.Pattern;
import de.metanome.algorithms.cfdfinder.pattern.PatternEntry;
import de.metanome.algorithms.cfdfinder.pattern.RangePatternEntry;

public class RangePatternExpansionStrategy extends ExpansionStrategy {

    private Map<Integer, List<Integer>> sortedClusterMaps;

    public RangePatternExpansionStrategy(int[][] values, List<Map<Integer, String>> clusterMaps) {
        super(values);
        sortedClusterMaps = new HashMap<>();
        int index = 0;
        for (Map<Integer, String> clusterMap : clusterMaps) {
            List<Map.Entry<Integer, String>> mappings = new ArrayList<>(clusterMap.entrySet());
            Collections.sort(mappings, new Comparator<Map.Entry<Integer, String>>() {
                @Override
                public int compare(Map.Entry<Integer, String> o1, Map.Entry<Integer, String> o2) {
                    if (o1 == null || o1.getKey() == null) {
                        return -1;
                    } else if (o2 == null || o2.getKey() == null) {
                        return 1;
                    } else {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                }
            });
            List<Integer> sortedClusters = new ArrayList<>(mappings.size());
            for (Map.Entry<Integer, String> entry : mappings) {
                sortedClusters.add(entry.getKey());
            }
            sortedClusterMaps.put(Integer.valueOf(index), sortedClusters);
            index += 1;
        }
    }

    public static String getIdentifier() {
        return "RangePatternStrategy";
    }

    @Override
    public Pattern generateNullPattern(BitSet attributes) {
        Map<Integer, PatternEntry> allVariables = new HashMap<>(attributes.cardinality());
        for (int i = attributes.nextSetBit(0); i >= 0; i = attributes.nextSetBit(i + 1)) {
            List<Integer> clusterMap = sortedClusterMaps.get(Integer.valueOf(i));
            allVariables.put(Integer.valueOf(i), new RangePatternEntry(clusterMap, 0, clusterMap.size() - 1));
        }
        return new Pattern(allVariables);
    }

    @Override
    public List<Pattern> getChildPatterns(Pattern currentPattern) {
        List<Pattern> result = new LinkedList<>();
        for (int i = 0; i < currentPattern.getIds().length; i+= 1) {
            Integer id = Integer.valueOf(currentPattern.getIds()[i]);

            HashMap<Integer, PatternEntry> lcopy = new HashMap<>(currentPattern.getAttributes());
            RangePatternEntry newEntry = ((RangePatternEntry) lcopy.get(id)).copy();
            boolean valid = newEntry.increaseLowerBound();
            if (valid) {
                lcopy.put(id, newEntry);
                result.add(new Pattern(lcopy));
            }

            HashMap<Integer, PatternEntry> rcopy = new HashMap<>(currentPattern.getAttributes());
            newEntry = ((RangePatternEntry) rcopy.get(id)).copy();
            valid = newEntry.decreaseUpperBound();
            if (valid) {
                rcopy.put(id, newEntry);
                result.add(new Pattern(rcopy));
            }
        }
        return result;
    }
}
