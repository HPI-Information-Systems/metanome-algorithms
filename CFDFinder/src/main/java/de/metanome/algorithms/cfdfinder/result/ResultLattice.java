package de.metanome.algorithms.cfdfinder.result;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import de.metanome.algorithms.cfdfinder.structures.FDTreeElement;
import de.metanome.algorithms.cfdfinder.utils.LhsUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;

public class ResultLattice {

    private int rhs;
    private Int2ObjectAVLTreeMap<List<Result>> layers = new Int2ObjectAVLTreeMap<>();

    public ResultLattice(int rhs) {
        this.rhs = rhs;
    }

    public ResultLattice(Result root) {
        this.rhs = root.getEmbeddedFD().rhs;
        this.insert(root);
    }

    public boolean insert(Result node) {
        if (valid(node)) {
            int l = node.getEmbeddedFD().lhs.cardinality();
            if (! layers.containsKey(l)) {
                layers.put(l, new LinkedList<Result>());
            }
            if (!layers.get(l).contains(node)) {
                layers.get(l).add(node);
            }
            return true;
        }
        return false;
    }

    public List<Result> getLayer(int i) {
        return layers.get(i);
    }

    public int size() {
        int size = 0;
        for (int i : layers.keySet()) {
            size += layers.get(i).size();
        }
        return size;
    }

    public boolean contains(BitSet lhs, int rhs) {
        return valid(rhs) && layerContains(lhs.cardinality(), lhs);
    }

    public boolean contains(Result node) {
        FDTreeElement.InternalFunctionalDependency fd = node.getEmbeddedFD();
        return valid(node) && layerContains(fd.lhs.cardinality(), fd.lhs);
    }

    private boolean valid(int rhs) {
        return rhs == this.rhs;
    }

    private boolean valid(Result node) {
        return valid(node.getEmbeddedFD().rhs);
    }

    private boolean layerContains(int n, BitSet lhs) {
        List<Result> layer = layers.get(n);
        if (layer != null) {
            for (Result result : layer) {
                if (result.getEmbeddedFD().lhs.equals(lhs)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Result> getParents(Result child) {
        List<Result> results = new LinkedList<>();
        int l = child.getEmbeddedFD().lhs.cardinality() + 1;
        if (layers.containsKey(l)) {
            for (BitSet lhs : LhsUtils.generateLhsSupersets(child.getEmbeddedFD().lhs, child.getEmbeddedFD().numAttributes)) {
                for (Result r : layers.get(l)) {
                    if (r.getEmbeddedFD().lhs.equals(lhs)) {
                        results.add(r);
                    }
                }
            }
        }
        return results;
    }

    public List<Result> getChildren(Result node) {
        List<Result> results = new LinkedList<>();
        int l = node.getEmbeddedFD().lhs.cardinality() - 1;
        if (layers.containsKey(l)) {
            for (BitSet lhs : LhsUtils.generateLhsSubsets(node.getEmbeddedFD().lhs)) {
                for (Result r : layers.get(l)) {
                    if (r.getEmbeddedFD().lhs.equals(lhs)) {
                        results.add(r);
                    }
                }
            }
        }
        return results;
    }

    public List<Result> getLeaves() {
        List<Result> results = new LinkedList<>();
        for (int i : layers.keySet()) {
            for (Result entry : layers.get(i)) {
                if (getChildren(entry).size() == 0) {
                    results.add(entry);
                }
            }
        }
        return results;
    }
}
