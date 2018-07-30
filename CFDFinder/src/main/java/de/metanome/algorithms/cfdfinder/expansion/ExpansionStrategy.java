package de.metanome.algorithms.cfdfinder.expansion;

import de.metanome.algorithms.cfdfinder.pattern.Pattern;
import org.apache.lucene.util.OpenBitSet;

import java.util.List;

public abstract class ExpansionStrategy {

    protected int[][] values;

    public ExpansionStrategy(int[][] values) {
        this.values = values;
    }

    public abstract Pattern generateNullPattern(OpenBitSet attributes);
    public abstract List<Pattern> getChildPatterns(Pattern currentPattern);
}
