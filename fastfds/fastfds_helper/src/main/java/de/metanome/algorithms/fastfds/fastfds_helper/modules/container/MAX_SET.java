package de.metanome.algorithms.fastfds.fastfds_helper.modules.container;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import de.metanome.algorithms.fastfds.fastfds_helper.util.BitSetUtil;

public class MAX_SET extends CMAX_SET {

    private boolean finalized;

    public MAX_SET(int attribute) {
        super(attribute);
        this.finalized = false;
    }

    @Override
    public String toString_() {

        String s = "max(" + this.attribute + ": ";
        for (BitSet set : this.columnCombinations) {
            s += BitSetUtil.convertToIntList(set);
        }
        return s + ")";
    }

    @Override
    public void finalize() {

        if (!this.finalized) {
            this.checkContentForOnlySuperSets();
        }
        this.finalized = true;

    }

    private void checkContentForOnlySuperSets() {

        List<BitSet> superSets = new LinkedList<BitSet>();
        List<BitSet> toDelete = new LinkedList<BitSet>();
        boolean toAdd = true;

        for (BitSet set : this.columnCombinations) {
            for (BitSet superSet : superSets) {
                if (this.checkIfSetIsSuperSetOf(set, superSet)) {
                    toDelete.add(superSet);
                }
                if (toAdd) {
                    toAdd = !this.checkIfSetIsSuperSetOf(superSet, set);
                }
            }
            superSets.removeAll(toDelete);
            if (toAdd) {
                superSets.add(set);
            } else {
                toAdd = true;
            }
            toDelete.clear();
        }

        // List<BitSet> superSets = new LinkedList<BitSet>();
        // for (BitSet toCheck : this.columnCombinations) {
        // boolean foundNewSuper = false;
        // BitSet foundedOldSuper = null;
        // boolean addToSuperSet = true;
        // for (BitSet superSet : superSets) {
        // if (toCheck.containsAll(superSet)) {
        // foundNewSuper = true;
        // foundedOldSuper = superSet;
        // break;
        // }
        // if (superSet.containsAll(toCheck)) {
        // addToSuperSet = false;
        // break;
        // }
        // }
        // if (foundNewSuper) {
        // superSets.remove(foundedOldSuper);
        // }
        // if (addToSuperSet) {
        // superSets.add(toCheck);
        // }
        // }

        this.columnCombinations = superSets;
    }

    private boolean checkIfSetIsSuperSetOf(BitSet set, BitSet set2) {
        BitSet setCopy = (BitSet) set.clone();
        setCopy.and(set2);
        return setCopy.equals(set2);
    }
}
