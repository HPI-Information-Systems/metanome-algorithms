package de.metanome.algorithms.fastfds.fastfds_helper.modules.container;

import java.util.BitSet;

import de.metanome.algorithms.fastfds.fastfds_helper.util.BitSetUtil;

public class AgreeSet extends StorageSet {

    protected BitSet attributes = new BitSet();

    public void add(int attribute) {

        this.attributes.set(attribute);
    }

    public BitSet getAttributes() {

        return (BitSet) this.attributes.clone();

    }

    @Override
    public String toString_() {

        return "ag(" + BitSetUtil.convertToIntList(this.attributes).toString()
                + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((attributes == null) ? 0 : attributes.hashCode());
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
        AgreeSet other = (AgreeSet) obj;
        if (attributes == null) {
            if (other.attributes != null)
                return false;
        } else if (!attributes.equals(other.attributes))
            return false;
        return true;
    }

}
