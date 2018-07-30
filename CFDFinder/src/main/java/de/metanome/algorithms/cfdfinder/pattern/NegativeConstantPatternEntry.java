package de.metanome.algorithms.cfdfinder.pattern;

public class NegativeConstantPatternEntry extends ConstantPatternEntry {

    public NegativeConstantPatternEntry(int constant) {
        super(constant);
    }

    @Override
    public String toString() {
        return "¬" + super.toString();
    }

    @Override
    boolean matches(int value) {
        return this.getConstant() != value;
    }

    @Override
    public int hashCode() {
        return -331 * super.hashCode();
    }
}
