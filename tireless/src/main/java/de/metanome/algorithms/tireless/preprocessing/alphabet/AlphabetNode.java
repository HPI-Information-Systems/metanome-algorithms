package de.metanome.algorithms.tireless.preprocessing.alphabet;

import java.util.*;

public class AlphabetNode extends Alphabet {
    private final List<Alphabet> subclasses;

    public AlphabetNode(String representingString) {
        super(representingString);
        subclasses = new ArrayList<>();
    }

    @Override
    public void resetLevel(int level) {
        this.level = level;
        for (Alphabet subclass : subclasses)
            subclass.resetLevel(this.level + 1);
    }

    @Override
    public BitSet getRepresentingBitset() {
        BitSet set = new BitSet();
        for (Alphabet subclass : subclasses)
            set.or(subclass.getRepresentingBitset());
        return set;
    }

    @Override
    public Map<Character, Alphabet> getCharMap() {
        Map<Character, Alphabet> result = new HashMap<>();
        for (Alphabet subclass : subclasses)
            result.putAll(subclass.getCharMap());
        return result;

    }

    public void addSubclass(Alphabet subclass) {
        subclasses.add(subclass);
        subclass.setParent(this);
    }

    public int getDepth() {
        int max = 1;
        for (Alphabet subclass : subclasses)
            max = Math.max(max, subclass.getDepth() + 1);
        return max;
    }

    public List<Alphabet> getSubclasses() {
        return subclasses;
    }

    @Override
    public boolean isLeaf() { return false; }
}
