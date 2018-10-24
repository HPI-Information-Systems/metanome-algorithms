package de.metanome.algorithms.fastfds.fastfds_helper.modules.container;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntList;

public class StrippedPartition extends StorageSet {

    protected int attribute;
    protected List<IntList> value = new LinkedList<IntList>();
    protected boolean finalized = false;

    public StrippedPartition(int attribute) {

        this.attribute = attribute;
    }

    public void addElement(IntList element) {

        if (finalized) {
            return;
        }
        this.value.add(element);
    }

    public void finalize() {

        this.finalized = true;
    }

    public int getAttributeID() {

        return this.attribute;
    }

    public List<IntList> getValues() {

        return this.value;

        // if (finalized) {
        // // TODO: notwendig?
        // List<IntList> temp = new LinkedList<IntList>();
        // for (IntList il : this.value) {
        // IntList temp_ = new LongArrayList();
        // temp_.addAll(il);
        // temp.add(temp_);
        // }
        // return temp;
        // } else {
        // return this.value;
        // }
    }

    public List<BitSet> getValuesAsBitSet() {

        List<BitSet> result = new LinkedList<BitSet>();
        for (IntList list : this.value) {
            BitSet set = new BitSet();
            for (int i : list) {
                set.set(i);
            }
            result.add(set);
        }
        return result;
    }

    @Override
    protected String toString_() {

        String s = "sp(";
        for (IntList il : this.value) {
            s += il.toString() + "-";
        }
        return s + ")";
    }

    public StrippedPartition copy() {
        StrippedPartition copy = new StrippedPartition(this.attribute);
        for (IntList l : this.value) {
            copy.value.add(l);
        }
        copy.finalized = this.finalized;
        return copy;
    }
}
