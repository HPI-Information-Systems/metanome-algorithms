package de.metanome.algorithms.fastfds.fastfds_helper.modules.container;

import it.unimi.dsi.fastutil.longs.LongList;
import org.apache.lucene.util.OpenBitSet;

import java.util.LinkedList;
import java.util.List;

public class StrippedPartition extends StorageSet {

    protected int attribute;
    protected List<LongList> value = new LinkedList<LongList>();
    protected boolean finalized = false;

    public StrippedPartition(int attribute) {

        this.attribute = attribute;
    }

    public void addElement(LongList element) {

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

    public List<LongList> getValues() {

        return this.value;

        // if (finalized) {
        // // TODO: notwendig?
        // List<LongList> temp = new LinkedList<LongList>();
        // for (LongList il : this.value) {
        // LongList temp_ = new LongArrayList();
        // temp_.addAll(il);
        // temp.add(temp_);
        // }
        // return temp;
        // } else {
        // return this.value;
        // }
    }

    public List<OpenBitSet> getValuesAsBitSet() {

        List<OpenBitSet> result = new LinkedList<OpenBitSet>();
        for (LongList list : this.value) {
            OpenBitSet set = new OpenBitSet();
            for (long i : list) {
                set.set(i);
            }
            result.add(set);
        }
        return result;
    }

    @Override
    protected String toString_() {

        String s = "sp(";
        for (LongList il : this.value) {
            s += il.toString() + "-";
        }
        return s + ")";
    }

    public StrippedPartition copy() {
        StrippedPartition copy = new StrippedPartition(this.attribute);
        for (LongList l : this.value) {
            copy.value.add(l);
        }
        copy.finalized = this.finalized;
        return copy;
    }
}
