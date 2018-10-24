package de.metanome.algorithms.fastfds.modules.container;

import java.util.BitSet;

import de.metanome.algorithms.fastfds.fastfds_helper.modules.container.AgreeSet;
import de.metanome.algorithms.fastfds.fastfds_helper.util.BitSetUtil;

public class DifferenceSet extends AgreeSet {

    public DifferenceSet(BitSet obs) {

        this.attributes = obs;
    }

    public DifferenceSet() {

        this(new BitSet());
    }

    @Override
    public String toString_() {

        return "diff(" + BitSetUtil.convertToIntList(this.attributes).toString()
                + ")";
    }
}
