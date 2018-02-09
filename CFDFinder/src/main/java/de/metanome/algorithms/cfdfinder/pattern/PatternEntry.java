package de.metanome.algorithms.cfdfinder.pattern;

public abstract class PatternEntry {

    abstract boolean matches(final int value);
    abstract boolean isVariable();

}
