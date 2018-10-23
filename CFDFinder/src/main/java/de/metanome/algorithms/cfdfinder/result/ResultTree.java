package de.metanome.algorithms.cfdfinder.result;

import static de.metanome.algorithms.cfdfinder.utils.LhsUtils.generateLhsSupersets;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import de.metanome.algorithms.cfdfinder.structures.FDTreeElement;

public class ResultTree {

    private Result node;
    private List<ResultTree> children;

    public ResultTree(Result root) {
        this.node = root;
        this.children = new LinkedList<>();
    }

    public Result getNode() {
        return node;
    }

    public void setNode(Result node) {
        this.node = node;
    }

    public List<ResultTree> getChildren() {
        return children;
    }

    public void setChildren(List<ResultTree> children) {
        this.children = children;
    }

    public boolean insert(Result result) {
        ResultTree parent = getInsertPosition(result);
        if (parent != null) {
            parent.getChildren().add(new ResultTree(result));
            return true;
        }
        return false;
    }

    public ResultTree getInsertPosition(Result result) {
        BitSet parent = findParentOf(result);
        if (parent != null) {
            ResultTree child = findNode(parent, result.getEmbeddedFD().rhs);
            assert child != null;
            return child;
        }
        return null;
    }

    private BitSet findParentOf(Result result) {
        for (BitSet parent : generateLhsSupersets(result.getEmbeddedFD().lhs, result.getEmbeddedFD().numAttributes)) {
            if (this.contains(parent, result.getEmbeddedFD().rhs)) {
                return parent;
            }
        }
        return null;
    }

    public boolean contains(BitSet lhs, int rhs) {
        return (findNode(lhs, rhs) != null);
    }

    private ResultTree findNode(BitSet lhs, int rhs) {
        FDTreeElement.InternalFunctionalDependency fd = node.getEmbeddedFD();
        if (fd.rhs != rhs) {
            return null;
        } else if (fd.lhs.equals(lhs)) {
            return this;
        } else {
            for (ResultTree child : this.children) {
                ResultTree result = child.findNode(lhs, rhs);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public List<Result> getLeaves() {
        List<Result> leaves = new LinkedList<>();
        appendLeaves(this, leaves);
        return leaves;
    }

    private void appendLeaves(ResultTree root, List<Result> results) {
        if (root.getChildren().size() == 0) {
            results.add(root.getNode());
        } else {
            for (ResultTree tree : root.children) {
                appendLeaves(tree, results);
            }
        }
    }

}
