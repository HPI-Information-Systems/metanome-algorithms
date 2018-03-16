package de.metanome.algorithms.cfdfinder.pattern;

import de.metanome.algorithms.cfdfinder.pattern.Pattern;
import de.metanome.algorithms.cfdfinder.pattern.PatternEntry;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UpdateCoverTest {

    private int[] a = new int[] {1, 2, 3, 4, 5, 6};
    private int[] b = new int[] {7, 8, 9};
    private int[] c = new int[] {1, 2};
    private int[] d = new int[] {4, 5, 6};
    private int[] e = new int[] {1, 2, 5, 6};

    @Test
    public void testUpdateCover() {
        Pattern pattern1 = initPattern(withCover(new int[][] {a, b}));
        pattern1.updateCover(initPattern(withCover(new int[][] {c, d})));
        Assert.assertEquals(2, pattern1.getCover().size());
        Assert.assertEquals(4, pattern1.getNumCover());

        Pattern pattern2 = initPattern(withCover(new int[][] {b, c, d}));
        pattern2.updateCover(initPattern(withCover(new int[][] {a})));
        Assert.assertEquals(1, pattern2.getCover().size());
        Assert.assertEquals(3, pattern2.getNumCover());
    }

    @Test
    public void testUpdateCoverEmptyResult() {
        Pattern pattern = initPattern(withCover(new int[][] {e}));
        pattern.updateCover(initPattern(withCover(new int[][] {c, d})));
        Assert.assertEquals(0, pattern.getCover().size());
        Assert.assertEquals(0, pattern.getNumCover());
    }

    @Test
    public void testUpdateCoverWithEmptyCandidatePatternCover() {
        // update a cover with an empty one
        Pattern pattern = initPattern(withCover(new int[][] {b}));
        pattern.updateCover(initPattern(withCover(new int[][] {})));
        Assert.assertEquals(1, pattern.getCover().size());
        Assert.assertEquals(3, pattern.getNumCover());
    }

    @Test
    public void testUpdateCoverEmpty() {
        // update an already empty cover with a filled one
        Pattern pattern = initPattern(withCover(new int[][] {}));
        pattern.updateCover(initPattern(withCover(new int[][] {a, b})));
        Assert.assertEquals(0, pattern.getCover().size());
        Assert.assertEquals(0, pattern.getNumCover());
    }

    private Pattern initPattern(List<IntArrayList> cover) {
        Pattern pattern = new Pattern(new HashMap<Integer, PatternEntry>());
        pattern.setCover(new ArrayList<>(cover));
        return pattern;
    }

    private List<IntArrayList> withCover(int[][] clusters) {
        List<IntArrayList> cover = new ArrayList<>(clusters.length);
        for (int[] cluster : clusters) {
            cover.add(new IntArrayList(cluster.clone()));
        }
        return cover;
    }
}
