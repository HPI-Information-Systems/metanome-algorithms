package de.metanome.algorithms.cfdfinder.pruning;

import de.metanome.algorithms.cfdfinder.pattern.Pattern;
import de.metanome.algorithms.cfdfinder.pattern.PatternTableau;
import de.metanome.algorithms.cfdfinder.structures.FDTreeElement;

import java.util.Set;

public interface PruningStrategy {
    void startNewTableau(FDTreeElement.InternalFunctionalDependency candidate);

    void finishTableau(PatternTableau tableau);

    void addPattern(Pattern pattern);

    void expandPattern(Pattern pattern);

    void processChild(Pattern child);

    boolean hasEnoughPatterns(Set<Pattern> tableau);

    boolean isPatternWorthConsidering(Pattern pattern);

    boolean isPatternWorthAdding(Pattern pattern);

    boolean validForProcessing(Pattern child);

    boolean continueGeneration(PatternTableau currentTableau);
}
