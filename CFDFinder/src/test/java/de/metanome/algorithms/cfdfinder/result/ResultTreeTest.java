package de.metanome.algorithms.cfdfinder.result;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.metanome.algorithms.cfdfinder.pattern.Pattern;
import de.metanome.algorithms.cfdfinder.pattern.PatternTableau;
import de.metanome.algorithms.cfdfinder.structures.FDTreeElement;

public class ResultTreeTest {

    @Test
    public void testSingleElementTree() {
        Result result = mockResult(mockFD(new int[]{0, 1, 2}, 3));
        ResultTree tree = new ResultTree(result);
        Assert.assertEquals(result, tree.getNode());
        Assert.assertEquals(0, tree.getChildren().size());
        Assert.assertTrue(tree.contains(result.getEmbeddedFD().lhs, result.getEmbeddedFD().rhs));
    }

    @Test
    public void testNesting() {
        Result node = mockResult(mockFD(new int[]{0, 1, 2}, 3));
        Result child = mockResult(mockFD(new int[]{0, 1}, 3));
        Assert.assertNotEquals(node, child);
        ResultTree tree = new ResultTree(node);
        boolean inserted = tree.insert(child);
        Assert.assertTrue(inserted);
        Assert.assertEquals(1, tree.getChildren().size());
        Assert.assertEquals(child, tree.getChildren().get(0).getNode());
    }
    
    @Test
    public void testInsertInNestedTree() {
        Result node = mockResult(mockFD(new int[]{0, 4, 7, 9}, 3));
        Result child = mockResult(mockFD(new int[]{4, 7, 9}, 3));
        Result grandchild = mockResult(mockFD(new int[] {4, 7}, 3));

        ResultTree tree = new ResultTree(node);
        boolean insertedChild = tree.insert(child);
        Assert.assertTrue(insertedChild);
        boolean insertedGrandchild = tree.insert(grandchild);
        Assert.assertTrue(insertedGrandchild);

        Assert.assertEquals(grandchild, tree.getChildren().get(0).getChildren().get(0).getNode());
    }

    @Test
    public void testInsertNotPossibleForDifferentRhs() {
        Result node = mockResult(mockFD(new int[]{0, 4, 7, 9}, 3));
        Result child = mockResult(mockFD(new int[]{4, 7, 9}, 3));
        Result offendingResult = mockResult(mockFD(new int[] {4, 7}, 1));

        ResultTree tree = new ResultTree(node);
        tree.insert(child);

        boolean inserted = tree.insert(offendingResult);
        Assert.assertFalse(inserted);
    }

    @Test
    public void testInsertNotPossibleForNonDescendants() {
        Result node = mockResult(mockFD(new int[]{0, 4, 7, 9}, 3));
        Result child = mockResult(mockFD(new int[]{4, 7, 9}, 3));
        Result offendingResult = mockResult(mockFD(new int[] {5, 7}, 3));

        ResultTree tree = new ResultTree(node);
        tree.insert(child);

        boolean inserted = tree.insert(offendingResult);
        Assert.assertFalse(inserted);
    }

    @Test
    public void testGetInsertPositionReturnsParentLHSInSimpleTree() {
        Result node = mockResult(mockFD(new int[]{0, 4, 7, 9}, 3));
        Result child = mockResult(mockFD(new int[]{4, 7, 9}, 3));
        Result grandchild = mockResult(mockFD(new int[] {4, 7}, 3));

        ResultTree tree = new ResultTree(node);
        boolean insertedChild = tree.insert(child);
        Assert.assertTrue(insertedChild);

        Assert.assertEquals(child, tree.getChildren().get(0).getNode());
        Assert.assertEquals(tree.getChildren().get(0), tree.getInsertPosition(grandchild));
    }

    @Test
    public void testGetInsertPositionReturnsParentLHSInBranchedTree() {
        Result node = mockResult(mockFD(new int[]{0, 4, 7, 9}, 3));
        Result child1 = mockResult(mockFD(new int[]{4, 7, 9}, 3));
        Result child2 = mockResult(mockFD(new int[]{0, 4, 7}, 3));
        Result child3 = mockResult(mockFD(new int[]{0, 7, 9}, 3));
        Result grandchild = mockResult(mockFD(new int[] {4, 7}, 3));

        ResultTree tree = new ResultTree(node);
        boolean insertedChild1 = tree.insert(child1);
        Assert.assertTrue(insertedChild1);
        boolean insertedChild2 = tree.insert(child2);
        Assert.assertTrue(insertedChild2);
        boolean insertedChild3 = tree.insert(child3);
        Assert.assertTrue(insertedChild3);

        Assert.assertEquals(child2, tree.getChildren().get(1).getNode());
        Assert.assertEquals(tree.getChildren().get(1), tree.getInsertPosition(grandchild));
    }

    @Test
    public void testGetLeavesReturnsNodeWhenTreeHeightIsOne() {
        Result result = mockResult(mockFD(new int[]{0, 1, 2}, 3));
        ResultTree tree = new ResultTree(result);
        Assert.assertEquals(1, tree.getLeaves().size());
        Assert.assertEquals(tree.getNode(), tree.getLeaves().get(0));
    }

    @Test
    public void testGetLeavesReturnsNodesWithNoChildren() {
        Result node = mockResult(mockFD(new int[]{0, 4, 7, 9}, 3));
        Result child = mockResult(mockFD(new int[]{4, 7, 9}, 3));
        Result grandchild1 = mockResult(mockFD(new int[] {4, 7}, 3));
        Result grandchild2 = mockResult(mockFD(new int[] {7, 9}, 3));

        ResultTree tree = new ResultTree(node);
        tree.insert(child);
        tree.insert(grandchild1);
        tree.insert(grandchild2);

        Assert.assertEquals(2, tree.getLeaves().size());
        Assert.assertEquals(grandchild1, tree.getLeaves().get(0));
        Assert.assertEquals(grandchild2, tree.getLeaves().get(1));
    }

    private Result mockResult(FDTreeElement.InternalFunctionalDependency fd) {
        return new Result(fd, new PatternTableau(new ArrayList<Pattern>(), 0),
                new ArrayList<String>(), new ArrayList<Map<Integer, String>>());
    }

    private FDTreeElement.InternalFunctionalDependency mockFD(int[] lhs, int rhs) {
        BitSet lhsSet = new BitSet(64);
        for (int lhsElem : lhs) {
            lhsSet.flip(lhsElem);
        }
        return new FDTreeElement.InternalFunctionalDependency(lhsSet, rhs, 64);
    }

}
