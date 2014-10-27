package de.metanome.algorithms.fastfds.modules.container;

import de.metanome.algorithms.fastfds.fastfds_helper.modules.container.AgreeSet;
import de.metanome.algorithms.fastfds.fastfds_helper.util.BitSetUtil;

import org.apache.lucene.util.OpenBitSet;

public class DifferenceSet extends AgreeSet {

    public DifferenceSet(OpenBitSet obs) {

        this.attributes = obs;
    }

    public DifferenceSet() {

        this(new OpenBitSet());
    }

    @Override
    public String toString_() {

        return "diff(" + BitSetUtil.convertToIntList(this.attributes).toString()
                + ")";
    }
}
