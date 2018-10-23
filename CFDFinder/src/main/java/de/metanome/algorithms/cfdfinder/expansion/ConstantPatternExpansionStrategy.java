package de.metanome.algorithms.cfdfinder.expansion;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.metanome.algorithms.cfdfinder.pattern.ConstantPatternEntry;
import de.metanome.algorithms.cfdfinder.pattern.Pattern;
import de.metanome.algorithms.cfdfinder.pattern.PatternEntry;
import de.metanome.algorithms.cfdfinder.pattern.VariablePatternEntry;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class ConstantPatternExpansionStrategy extends ExpansionStrategy {

    public ConstantPatternExpansionStrategy(int[][] values) {
        super(values);
    }

    public static String getIdentifier() {
        return "ConstantPatternStrategy";
    }

    @Override
    public Pattern generateNullPattern(BitSet attributes) {
        Map<Integer, PatternEntry> allVariables = new HashMap<>();
        for (int i = attributes.nextSetBit(0); i >= 0; i = attributes.nextSetBit(i + 1)) {
            allVariables.put(Integer.valueOf(i), new VariablePatternEntry());
        }
        return new Pattern(allVariables);
    }

    @Override
    public List<Pattern> getChildPatterns(Pattern currentPattern) {
        List<Pattern> result = new LinkedList<>();
        for (IntArrayList cluster : currentPattern.getCover()) {
            result.addAll(getChildPatterns(currentPattern, cluster));
        }
        return result;
    }

    private List<Pattern> getChildPatterns(Pattern pattern, IntArrayList cluster) {
        List<Pattern> results = new LinkedList<>();
        for (int i = 0; i < pattern.getIds().length; i+= 1) {
            int id = pattern.getIds()[i];
            PatternEntry entry = pattern.getPatternEntries()[i];
            if (entry instanceof VariablePatternEntry) {
                int value = values[cluster.getInt(0)][id];
                results.addAll(specializeVariablePatternEntry(pattern, id, value));
            }
        }
        return results;
    }

    protected List<Pattern> specializeVariablePatternEntry(Pattern parent, int id, int value) {
        ArrayList<Pattern> result = new ArrayList<>();
        HashMap<Integer, PatternEntry> copy = new HashMap<>(parent.getAttributes());
        copy.put(Integer.valueOf(id), new ConstantPatternEntry(value));
        result.add(new Pattern(copy));
        return result;
    }
}
