package de.metanome.algorithms.cfdfinder.pattern;

public class VariablePatternEntry extends PatternEntry {

    @Override
    public String toString() {
        return "_";
    }

    @Override
    boolean matches(final int value) {
        return true;
    }

    @Override
    boolean isVariable() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PatternEntry that = (PatternEntry) o;

        return that.isVariable();
    }

    @Override
    public int hashCode() {
        return -1;
    }
}
