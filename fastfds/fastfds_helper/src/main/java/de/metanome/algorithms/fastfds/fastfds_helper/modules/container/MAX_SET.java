package de.metanome.algorithms.fastfds.fastfds_helper.modules.container;

import de.metanome.algorithms.fastfds.fastfds_helper.util.BitSetUtil;

import org.apache.lucene.util.OpenBitSet;

import java.util.LinkedList;
import java.util.List;

public class MAX_SET extends CMAX_SET {

    private boolean finalized;

    public MAX_SET(int attribute) {
        super(attribute);
        this.finalized = false;
    }

    @Override
    public String toString_() {

        String s = "max(" + this.attribute + ": ";
        for (OpenBitSet set : this.columnCombinations) {
            s += BitSetUtil.convertToLongList(set);
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

        List<OpenBitSet> superSets = new LinkedList<OpenBitSet>();
        List<OpenBitSet> toDelete = new LinkedList<OpenBitSet>();
        boolean toAdd = true;

        for (OpenBitSet set : this.columnCombinations) {
            for (OpenBitSet superSet : superSets) {
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

        // List<OpenBitSet> superSets = new LinkedList<OpenBitSet>();
        // for (OpenBitSet toCheck : this.columnCombinations) {
        // boolean foundNewSuper = false;
        // OpenBitSet foundedOldSuper = null;
        // boolean addToSuperSet = true;
        // for (OpenBitSet superSet : superSets) {
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

    private boolean checkIfSetIsSuperSetOf(OpenBitSet set, OpenBitSet set2) {
        OpenBitSet setCopy = set.clone();
        setCopy.intersect(set2);
        return setCopy.equals(set2);
    }
}
