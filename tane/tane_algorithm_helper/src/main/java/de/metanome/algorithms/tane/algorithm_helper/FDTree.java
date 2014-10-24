package de.metanome.algorithms.tane.algorithm_helper;

import org.apache.lucene.util.OpenBitSet;

public class FDTree extends FDTreeElement {

    public FDTree(int maxAttributeNumber) {
        super(maxAttributeNumber);
    }

    public void addMostGeneralDependencies() {
        this.rhsAttributes.set(1, maxAttributeNumber + 1);
        for (int i = 0; i < maxAttributeNumber; i++) {
            isfd[i] = true;
        }
    }

    public void addFunctionalDependency(OpenBitSet lhs, int a) {
        FDTreeElement fdTreeEl;
        //update root vertex
        FDTreeElement currentNode = this;
        currentNode.addRhsAttribute(a);

        for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {

            if (currentNode.children[i - 1] == null) {
                fdTreeEl = new FDTreeElement(maxAttributeNumber);
                currentNode.children[i - 1] = fdTreeEl;
            }
            // update vertex to add attribute
            currentNode = currentNode.getChild(i - 1);
            currentNode.addRhsAttribute(a);
        }
        // mark the last element
        currentNode.markAsLastVertex(a - 1);
    }

    public boolean isEmpty() {
        return (rhsAttributes.cardinality() == 0);
    }

    /**
     * @return
     */
    public void filterSpecializations() {
        OpenBitSet activePath = new OpenBitSet();
        FDTree filteredTree = new FDTree(maxAttributeNumber);
        this.filterSpecializations(filteredTree, activePath);

        this.children = filteredTree.children;
        this.isfd = filteredTree.isfd;

    }

    public void filterGeneralizations() {
        OpenBitSet activePath = new OpenBitSet();
        FDTree filteredTree = new FDTree(maxAttributeNumber);
        this.filterGeneralizations(filteredTree, activePath);

        this.children = filteredTree.children;
    }

    public void printDependencies() {
        OpenBitSet activePath = new OpenBitSet();
        this.printDependencies(activePath);

    }

}
