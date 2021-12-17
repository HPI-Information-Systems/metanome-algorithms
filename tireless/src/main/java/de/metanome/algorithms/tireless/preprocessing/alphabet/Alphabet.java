package de.metanome.algorithms.tireless.preprocessing.alphabet;

import java.util.BitSet;
import java.util.Map;

public abstract class Alphabet {
    private AlphabetNode parent;
    private final String representingString;
    protected int level;

    public Alphabet(String representingString) {
        parent = null;
        level = 0;
        this.representingString = representingString;
    }

    public void setParent(AlphabetNode alphabet) {
        parent = alphabet;
    }

    public Alphabet getSuperclassOfLevel(int level) {
        return (getLevel() <= level || parent == null) ? this : parent.getSuperclassOfLevel(level);
    }

    public int getLevel() {
        return level;
    }

    public Alphabet getParent() {
        return parent;
    }

    public abstract void resetLevel(int level);

    public abstract BitSet getRepresentingBitset();

    public abstract Map<Character, Alphabet> getCharMap();

    public abstract int getDepth();

    public abstract boolean isLeaf();

    public String getRepresentingString() {
        return representingString;
    }

}
