package de.metanome.algorithms.fastfds.fastfds_helper.util;

import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class BitSetUtil {

    public static IntList convertToIntList(BitSet set) {
        IntList bits = new IntArrayList();
        int lastIndex = set.nextSetBit(0);
        while (lastIndex != -1) {
            bits.add(lastIndex);
            lastIndex = set.nextSetBit(lastIndex + 1);
        }
        return bits;
    }

    public static BitSet convertToBitSet(IntList list) {
        BitSet set = new BitSet(list.size());
        for (int l : list) {
            set.set(l);
        }
        return set;
    }
}
