package de.metanome.algorithms.cfdfinder.expansion;

import java.util.BitSet;
import java.util.List;

import de.metanome.algorithms.cfdfinder.pattern.Pattern;

public abstract class ExpansionStrategy {

    protected int[][] values;

    public ExpansionStrategy(int[][] values) {
        this.values = values;
    }

    public abstract Pattern generateNullPattern(BitSet attributes);
    public abstract List<Pattern> getChildPatterns(Pattern currentPattern);
}
