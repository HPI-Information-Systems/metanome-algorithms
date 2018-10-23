package de.metanome.algorithms.cfdfinder.expansion;

import de.metanome.algorithms.cfdfinder.pattern.NegativeConstantPatternEntry;
import de.metanome.algorithms.cfdfinder.pattern.Pattern;
import de.metanome.algorithms.cfdfinder.pattern.PatternEntry;

import java.util.HashMap;
import java.util.List;

public class PositiveAndNegativeConstantPatternExpansionStrategy extends ConstantPatternExpansionStrategy {

    public PositiveAndNegativeConstantPatternExpansionStrategy(int[][] values) {
        super(values);
    }

    public static String getIdentifier() {
        return "PositiveAndNegativeConstantPatternStrategy";
    }

    @Override
    protected List<Pattern> specializeVariablePatternEntry(Pattern parent, int id, int value) {
        List<Pattern> specializations = super.specializeVariablePatternEntry(parent, id, value);
        HashMap<Integer, PatternEntry> copy = new HashMap<>(parent.getAttributes());
        copy.put(Integer.valueOf(id), new NegativeConstantPatternEntry(value));
        specializations.add(new Pattern(copy));
        return specializations;
    }
}
