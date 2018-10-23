package de.metanome.algorithms.tane.algorithm_helper;

import java.util.BitSet;

public class FDTreeElement {
    protected FDTreeElement[] children;
    protected BitSet rhsAttributes;
    protected boolean[] isfd;
    protected int maxAttributeNumber;

    public FDTreeElement(int maxAttributeNumber) {
        this.maxAttributeNumber = maxAttributeNumber;
        children = new FDTreeElement[maxAttributeNumber];
        isfd = new boolean[maxAttributeNumber];
        rhsAttributes = new BitSet();
    }


    /**
     * Return true, if the i'th attribute of the rhs attributes is an fd.
     * false, otherwise.
     *
     * @param i
     * @return
     */
    public boolean isFd(int i) {
        return isfd[i];
    }

    /**
     * Return the i'th child.
     *
     * @param i the i'th child. Possible values from 0 to maxAttributeNumber-1
     * @return
     */
    public FDTreeElement getChild(int i) {
        return children[i];
    }


    public void addRhsAttribute(int i) {
        rhsAttributes.set(i);
    }

    /**
     * Get a set of attributes, which are a dependent in the current node or in one of the subtrees.
     *
     * @return The right-hand-side attributes.
     */
    public BitSet getRhsAttributes() {
        return rhsAttributes;
    }

    /**
     * Mark the i'th attribute as a dependent of a valid functional dependency.
     *
     * @param i
     */
    public void markAsLastVertex(int i) {
        isfd[i] = true;
    }

    /**
     * Checks, whether the dependent attribute ends in the current tree element.
     *
     * @param a the i'th dependent attribute.
     * @return true, if the tree element does not have any children with the same dependent attribute.
     * false, otherwise.
     */
    public boolean isFinalNode(int a) {
        int attr;
        if (this.rhsAttributes.get(a)) {
            // Check all children for right-hand-side attribute a.
            for (attr = 1; attr <= maxAttributeNumber; attr++) {
                if (children[attr - 1] != null) {
                    if (children[attr - 1].getRhsAttributes().get(a)) {
                        return false;
                    }
                }
            }
            return true;
        }
		return false;
    }

    /**
     * Return, whether the tree element contains a specialization of the functional dependency lhs -> a. </br>
     *
     * @param lhs         The left-hand-side attribute set of the functional dependency.
     * @param a           The dependent attribute.
     * @param currentAttr The last attribute from the left-hand-side attributes, which has already been checked.
     *                    This attribute and all smaller ones have already been found in the path. </br>
     *                    Only use values from 0 to maxAttributeNumber.
     *                    0 is only used if no attribute is checked yet.
     * @return true, if the element contains a specialization of the functional dependency lhs -> a.
     * false otherwise.
     */
    public boolean containsSpecialization(BitSet lhs, int a, int currentAttr) {
        if (!rhsAttributes.get(a)) {
            return false;
        }
        // is the dependency already covered?
        int nextSetAttribute = lhs.nextSetBit(currentAttr + 1);
        if (nextSetAttribute < 0) {
            return true;
        }
        // attributes start with 1.
        int attr = Math.max(currentAttr, 1);

        boolean found = false;
        while (!found && attr <= nextSetAttribute) {
            if (children[attr - 1] != null) {
                if (children[attr - 1].getRhsAttributes().get(a)) {
                    if (attr < nextSetAttribute) {
                        // Try next vertex with currentAttr.
                        found = children[attr - 1].containsSpecialization(lhs, a, currentAttr);
                    } else if (attr == nextSetAttribute) {
                        // Found nextSetAttribute in the path. Check child for the following set attribute.
                        found = children[nextSetAttribute - 1].containsSpecialization(lhs, a, nextSetAttribute);
                    }
                }
            }
            attr++;
        }
        return found;
    }


    /**
     * Check, whether the tree element contains a generalization of the dependency lhs -> a.
     *
     * @param lhs
     * @param a
     * @param currentAttr
     * @return true, if the tree element contains generalization,
     * false otherwise.
     */
    public boolean containsGeneralization(BitSet lhs, int a, int currentAttr) {
        if (this.isfd[a - 1]) {
            return true;
        }

        int nextSetAttr = lhs.nextSetBit(currentAttr + 1);
        if (nextSetAttr < 0) {
            return false;
        }

        boolean found = false;
        if (this.children[nextSetAttr - 1] != null) {
            if (this.children[nextSetAttr - 1].getRhsAttributes().get(a)) {
                found = children[nextSetAttr - 1].containsGeneralization(lhs, a, nextSetAttr);
            }
        }
        if (!found) {
            return this.containsGeneralization(lhs, a, nextSetAttr);
        }
		return true;
    }

    /**
     * Returns a specialization of the given functional dependency.
     * Only the lhs is returned. The rhs is the same = a.
     *
     * @param lhs
     * @param a
     * @param currentAttr
     * @param specLhsOut  The BitSet for the result. It must be empty.
     * @return true, if the tree contains a specialization,
     * false otherwise.
     */
    public boolean getSpecialization(BitSet lhs, int a, int currentAttr, BitSet specLhsOut) {
        int nextSetAttr, attr;
        boolean found = false;
//		if (!specLhsOut.isEmpty()) {
//			specLhsOut.clear(0, this.maxAttributeNumber);
//		}


        if (!this.rhsAttributes.get(a)) {
            return false;
        }

        attr = Math.max(currentAttr, 1);

        nextSetAttr = lhs.nextSetBit(currentAttr + 1);
        if (nextSetAttr < 0) {
            //
            while (!found && attr <= this.maxAttributeNumber) {
                if (this.children[attr - 1] != null) {
                    if (this.children[attr - 1].getRhsAttributes().get(a)) {
                        found = this.children[attr - 1].getSpecialization(lhs, a, currentAttr, specLhsOut);
                    }
                }
                attr++;
            }
            if (found) {
                specLhsOut.set(attr - 1);
            }
            return true;
        }

        while (!found && attr <= nextSetAttr) {
            if (children[attr - 1] != null) {
                if (this.children[attr - 1].getRhsAttributes().get(a)) {
                    if (attr < nextSetAttr) {
                        found = this.children[attr - 1].getSpecialization(lhs, a, currentAttr, specLhsOut);
                    } else {
                        found = this.children[nextSetAttr - 1].getSpecialization(lhs, a, nextSetAttr, specLhsOut);
                    }
                }
            }
            attr++;
        }

        if (found) {
            specLhsOut.set(attr - 1);
        }

        return found;
    }


    /**
     * Delete a generalization if it exists.
     *
     * @param lhs
     * @param a
     * @param currentAttr
     * @return true, if a generalization has been found and deleted. </br>
     * false otherwise.
     */
    public boolean deleteGeneralizations(BitSet lhs, int a, int currentAttr) {
        boolean found = false;
        int nextSetAttr;
        if (this.isfd[a - 1]) {
            this.isfd[a - 1] = false;
            this.rhsAttributes.clear(a);
            return true;
        }
        nextSetAttr = lhs.nextSetBit(currentAttr + 1);
        if (nextSetAttr < 0) {
            return false;
        }
        int i = currentAttr + 1;
        for (; nextSetAttr >= 0; i++, nextSetAttr = lhs.nextSetBit(i)) {
            if (this.children[nextSetAttr - 1] != null) {
                if (this.children[nextSetAttr - 1].getRhsAttributes().get(a)) {
                    boolean newFound = this.children[nextSetAttr - 1].deleteGeneralizations(lhs, a, nextSetAttr);
                    if (!found) {
                        found = newFound;
                    }
                    if (newFound) {
                        if (this.isFinalNode(a)) {
                            this.rhsAttributes.clear(a);
                        }
                    }
                }
            }
        }
//		if (!found) {
//			found = this.deleteGeneralizations(lhs, a, nextSetAttr);
//		}

        return found;
    }


    public boolean getGeneralizationAndDelete(BitSet lhs, int a, int currentAttr, BitSet specLhs) {
        boolean found = false;
        int nextSetAttr;
        if (this.isfd[a - 1]) {
            this.isfd[a - 1] = false;
            this.rhsAttributes.clear(a);
            return true;
        }
        nextSetAttr = lhs.nextSetBit(currentAttr + 1);
        if (nextSetAttr < 0) {
            return false;
        }

        if (this.children[nextSetAttr - 1] != null) {
            if (this.children[nextSetAttr - 1].getRhsAttributes().get(a)) {
                found = this.children[nextSetAttr - 1].getGeneralizationAndDelete(lhs, a, nextSetAttr, specLhs);
                if (found) {
                    if (this.isFinalNode(a)) {
                        this.rhsAttributes.clear(a);
                    }
                    specLhs.set(nextSetAttr);
                }
            }
        }
        if (!found) {
            found = this.getGeneralizationAndDelete(lhs, a, nextSetAttr, specLhs);
        }

        return found;
    }

    public void filterSpecializations(FDTree filteredTree, BitSet activePath) {
        int attr;
        for (attr = 1; attr <= maxAttributeNumber; attr++) {
            if (children[attr - 1] != null) {
                activePath.set(attr);
                children[attr - 1].filterSpecializations(filteredTree, activePath);
                activePath.clear(attr);
            }
        }

        for (attr = 1; attr <= maxAttributeNumber; attr++) {
            if (this.isfd[attr - 1]) {
                // TODO: containsSpecialization should be enough
                if (!filteredTree.getSpecialization(activePath, attr, 0, new BitSet())) {
                    filteredTree.addFunctionalDependency(activePath, attr);
                }
            }
        }
    }

    // Only keep the most general dependencies in the tree
    public void filterGeneralizations(FDTree filteredTree, BitSet activePath) {
        int attr;
        for (attr = 1; attr <= maxAttributeNumber; attr++) {
            if (isfd[attr - 1]) {
                if (!filteredTree.containsGeneralization(activePath, attr, 0)) {
                    filteredTree.addFunctionalDependency(activePath, attr);
                }
            }
        }
        for (attr = maxAttributeNumber; attr > 0; attr--) {
            if (children[attr - 1] != null) {
                activePath.set(attr);
                children[attr - 1].filterGeneralizations(filteredTree, activePath);
                activePath.clear(attr);
            }
        }
    }

    public void printDependencies(BitSet activePath) {

        for (int attr = 1; attr <= maxAttributeNumber; attr++) {
            if (isfd[attr - 1]) {
                String out = "{";
                for (int i = activePath.nextSetBit(0); i >= 0; i = activePath.nextSetBit(i + 1)) {
                    out += i + ",";
                }
                if (out.length() > 1) {
                    out = out.substring(0, out.length() - 1);
                }

                out += "} -> " + attr;
                System.out.println(out);
            }
        }

        for (int attr = 1; attr <= maxAttributeNumber; attr++) {
            if (children[attr - 1] != null) {
                activePath.set(attr);
                children[attr - 1].printDependencies(activePath);
                activePath.clear(attr);
            }
        }
    }
}
