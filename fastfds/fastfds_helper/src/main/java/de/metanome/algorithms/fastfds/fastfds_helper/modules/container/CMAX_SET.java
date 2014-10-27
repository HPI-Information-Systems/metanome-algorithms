package de.metanome.algorithms.fastfds.fastfds_helper.modules.container;

import de.metanome.algorithms.fastfds.fastfds_helper.util.BitSetUtil;

import org.apache.lucene.util.OpenBitSet;

import java.util.LinkedList;
import java.util.List;

public class CMAX_SET extends StorageSet {

    protected int attribute;
    protected List<OpenBitSet> columnCombinations;
    private boolean finalized;

    public CMAX_SET(int attribute) {

        this.attribute = attribute;
        this.columnCombinations = new LinkedList<OpenBitSet>();
        this.finalized = false;
    }

    public void addCombination(OpenBitSet combination) {

        this.columnCombinations.add(combination);
    }

    public List<OpenBitSet> getCombinations() {

        return this.columnCombinations;
    }

    public int getAttribute() {

        return this.attribute;
    }

    @Override
    public String toString_() {

        String s = "cmax(" + this.attribute + ": ";
        for (OpenBitSet set : this.columnCombinations) {
            s += BitSetUtil.convertToLongList(set);
        }
        return s + ")";
    }

    public void finalize() {

        this.finalized = true;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + attribute;
        result = prime * result + ((columnCombinations == null) ? 0 : columnCombinations.hashCode());
        result = prime * result + (finalized ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CMAX_SET other = (CMAX_SET) obj;
        if (attribute != other.attribute)
            return false;
        if (columnCombinations == null) {
            if (other.columnCombinations != null)
                return false;
        } else if (!columnCombinations.equals(other.columnCombinations))
            return false;
        if (finalized != other.finalized)
            return false;
        return true;
    }

}
