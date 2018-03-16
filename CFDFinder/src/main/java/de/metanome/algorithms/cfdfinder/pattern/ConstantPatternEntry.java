package de.metanome.algorithms.cfdfinder.pattern;

public class ConstantPatternEntry extends PatternEntry {

    private int constant;

    public ConstantPatternEntry(int constant) {
        this.constant = constant;
    }

    public int getConstant() {
        return constant;
    }

    @Override
    public String toString() {
        return String.valueOf(constant);
    }

    @Override
    boolean matches(final int value) {
        return constant == value;
    }

    @Override
    boolean isVariable() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PatternEntry that = (PatternEntry) o;
        if (that.isVariable()) {
            return false;
        }

        ConstantPatternEntry it = (ConstantPatternEntry) o;
        return constant == it.constant;
    }

    @Override
    public int hashCode() {
        return 1 + constant;
    }
}
