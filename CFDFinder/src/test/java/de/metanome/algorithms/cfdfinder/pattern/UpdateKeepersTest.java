package de.metanome.algorithms.cfdfinder.pattern;

import de.metanome.algorithms.cfdfinder.pattern.ConstantPatternEntry;
import de.metanome.algorithms.cfdfinder.pattern.Pattern;
import de.metanome.algorithms.cfdfinder.pattern.PatternEntry;
import de.metanome.algorithms.cfdfinder.pattern.VariablePatternEntry;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateKeepersTest {

    private Pattern pattern;

    @Before
    public void setUp() throws Exception {
        int[] clusters = new int[] {1, 2, 5, 6};

        List<IntArrayList> cover = new ArrayList<>();
        cover.add(new IntArrayList(clusters));

        Map<Integer, PatternEntry> attributes = new HashMap<>();
        attributes.put(0, new VariablePatternEntry());
        attributes.put(1, new ConstantPatternEntry(2));
        attributes.put(2, new ConstantPatternEntry(4));

        pattern = new Pattern(attributes);
        pattern.setCover(cover);
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testUpdateKeepersAverageCase() {
        pattern.updateKeepers(new int[]{0, 1, 1, 3, 3, 1, 1}); // keepers: 1, 2, 5, 6
        Assert.assertEquals(4, pattern.getNumKeepers());
    }

    @Test
    public void testUpdateKeepersBiggerRhsCluster() {
        pattern.updateKeepers(new int[] {1, 1, 1, 1, 3, 1, 1}); // keepers: 1, 2, 5, 6
        Assert.assertEquals(4, pattern.getNumKeepers());
    }

    @Test
    public void testUpdateKeepersSmallerRhsCluster() {
        pattern.updateKeepers(new int[] {0, 1, 1, 3, 3, 4, 5}); // keepers: 1, 2
        Assert.assertEquals(2, pattern.getNumKeepers());

        pattern.updateKeepers(new int[] {1, 1, 0, 3, 3, 1, 1}); // keepers: 1, 5, 6
        Assert.assertEquals(3, pattern.getNumKeepers());
    }

    @Test
    public void testUpdateKeepersOneTuplePerCluster() {
        pattern.updateKeepers(new int[] {0, 1, 2, 3, 4, 5, 6}); // keepers: any single tuple
        Assert.assertEquals(1, pattern.getNumKeepers());
    }

    @Test
    public void testUpdateKeepersRhsIsKey() {
        pattern.updateKeepers(new int[] {-1, -1, -1, -1, -1, -1, -1}); // keepers: any single tuple (rhs is key)
        Assert.assertEquals(1, pattern.getNumKeepers());
    }

}
