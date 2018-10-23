package de.metanome.algorithms.cfdfinder.result;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.metanome.algorithms.cfdfinder.pattern.Pattern;
import de.metanome.algorithms.cfdfinder.pattern.PatternTableau;
import de.metanome.algorithms.cfdfinder.structures.FDTreeElement;

public class ResultLatticeTest {

    @Test
    public void testSingleElementTree() {
        Result result = mockResult(mockFD(new int[]{0, 1, 2}, 3));
        ResultLattice lattice = new ResultLattice(result);
        Assert.assertEquals(result, lattice.getLayer(3).get(0));
        Assert.assertEquals(1, lattice.size());
        Assert.assertTrue(lattice.contains(result.getEmbeddedFD().lhs, result.getEmbeddedFD().rhs));
        Assert.assertTrue(lattice.contains(result));
    }

    @Test
    public void testNesting() {
        Result node = mockResult(mockFD(new int[]{0, 1, 2}, 3));
        Result child = mockResult(mockFD(new int[]{0, 1}, 3));
        Assert.assertNotEquals(node, child);
        ResultLattice lattice = new ResultLattice(node);
        boolean inserted = lattice.insert(child);
        Assert.assertTrue(inserted);
        Assert.assertEquals(1, lattice.getLayer(3).size());
        Assert.assertEquals(1, lattice.getLayer(2).size());
        Assert.assertEquals(child, lattice.getLayer(2).get(0));
    }

    @Test
    public void testInsertInNestedTree() {
        Result node = mockResult(mockFD(new int[]{0, 4, 7, 9}, 3));
        Result child = mockResult(mockFD(new int[]{4, 7, 9}, 3));
        Result grandchild = mockResult(mockFD(new int[] {4, 7}, 3));

        ResultLattice lattice = new ResultLattice(node);
        boolean insertedChild = lattice.insert(child);
        Assert.assertTrue(insertedChild);
        boolean insertedGrandchild = lattice.insert(grandchild);
        Assert.assertTrue(insertedGrandchild);

        Assert.assertTrue(lattice.getLayer(2).contains(grandchild));
    }

    @Test
    public void testInsertDoesNotInsertTheSameNodeTwice() {
        Result node = mockResult(mockFD(new int[]{0, 4, 7, 9}, 3));

        ResultLattice lattice = new ResultLattice(node);
        Assert.assertEquals(1, lattice.size());

        boolean insertedNode = lattice.insert(node);

        Assert.assertTrue(insertedNode);
        Assert.assertEquals(1, lattice.size());
    }

    @Test
    public void testInsertNotPossibleForDifferentRhs() {
        Result node = mockResult(mockFD(new int[]{0, 4, 7, 9}, 3));
        Result child = mockResult(mockFD(new int[]{4, 7, 9}, 3));
        Result offendingResult = mockResult(mockFD(new int[] {4, 7}, 1));

        ResultLattice lattice = new ResultLattice(node);
        lattice.insert(child);

        boolean inserted = lattice.insert(offendingResult);
        Assert.assertFalse(inserted);
    }

    @Test
    public void testInsertPositionInSimpleTree() {
        Result node = mockResult(mockFD(new int[]{0, 4, 7, 9}, 3));
        Result child = mockResult(mockFD(new int[]{4, 7, 9}, 3));
        Result grandchild = mockResult(mockFD(new int[] {4, 7}, 3));

        ResultLattice lattice = new ResultLattice(node);
        boolean insertedChild = lattice.insert(child);
        Assert.assertTrue(insertedChild);

        Assert.assertEquals(node, lattice.getParents(child).get(0));
        Assert.assertEquals(child, lattice.getParents(grandchild).get(0));
    }

    @Test
    public void testGetInsertPositionReturnsParentLHSInBranchedTree() {
        Result node = mockResult(mockFD(new int[]{0, 4, 7, 9}, 3));
        Result child1 = mockResult(mockFD(new int[]{4, 7, 9}, 3));
        Result child2 = mockResult(mockFD(new int[]{0, 4, 7}, 3));
        Result child3 = mockResult(mockFD(new int[]{0, 7, 9}, 3));
        Result grandchild = mockResult(mockFD(new int[] {4, 7}, 3));

        ResultLattice lattice = new ResultLattice(node);
        boolean insertedChild1 = lattice.insert(child1);
        Assert.assertTrue(insertedChild1);
        boolean insertedChild2 = lattice.insert(child2);
        Assert.assertTrue(insertedChild2);
        boolean insertedChild3 = lattice.insert(child3);
        Assert.assertTrue(insertedChild3);

        List<Result> parents = lattice.getParents(grandchild);
        Assert.assertEquals(2, parents.size());
        Assert.assertTrue(parents.contains(child1));
        Assert.assertTrue(parents.contains(child2));
    }

    @Test
    public void testGetLeavesReturnsNodeWhenTreeHeightIsOne() {
        Result result = mockResult(mockFD(new int[]{0, 1, 2}, 3));
        ResultLattice lattice = new ResultLattice(result);
        Assert.assertEquals(1, lattice.getLeaves().size());
        Assert.assertEquals(result, lattice.getLeaves().get(0));
    }

    @Test
    public void testGetLeavesReturnsNodesWithNoChildren() {
        Result node = mockResult(mockFD(new int[]{0, 4, 7, 9}, 3));
        Result child = mockResult(mockFD(new int[]{4, 7, 9}, 3));
        Result grandchild1 = mockResult(mockFD(new int[] {4, 7}, 3));
        Result grandchild2 = mockResult(mockFD(new int[] {7, 9}, 3));

        ResultLattice lattice = new ResultLattice(node);
        lattice.insert(child);
        lattice.insert(grandchild1);
        lattice.insert(grandchild2);

        Assert.assertEquals(2, lattice.getLeaves().size());
        Assert.assertTrue(lattice.getLeaves().contains(grandchild1));
        Assert.assertTrue(lattice.getLeaves().contains(grandchild2));
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
