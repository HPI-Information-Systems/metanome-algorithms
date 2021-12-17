package de.metanome.algorithms.tireless.preprocessing.alphabet;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class AlphabetLeaf extends Alphabet{
    private final BitSet charSet;

    public AlphabetLeaf(BitSet charSet, String representingString) {
        super(representingString);
        this.charSet = charSet;
    }

    @Override
    public void resetLevel(int level) {
        this.level = level;
    }

    @Override
    public BitSet getRepresentingBitset() {
        return this.charSet;
    }

    @Override
    public Map<Character, Alphabet> getCharMap() {
        Map<Character, Alphabet> result = new HashMap<>();
        for (int i = charSet.nextSetBit(0); i >= 0; i = charSet.nextSetBit(i + 1))
            result.put((char) i, this);
        return result;
    }

    @Override
    public int getDepth() {
        return 1;
    }

    @Override
    public boolean isLeaf() { return true; }

}
