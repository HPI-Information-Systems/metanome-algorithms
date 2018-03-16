package de.metanome.algorithms.cfdfinder.expansion;

import de.metanome.algorithms.cfdfinder.pattern.ConstantPatternEntry;
import de.metanome.algorithms.cfdfinder.pattern.Pattern;
import de.metanome.algorithms.cfdfinder.pattern.PatternEntry;
import de.metanome.algorithms.cfdfinder.pattern.VariablePatternEntry;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.apache.lucene.util.OpenBitSet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantPatternExpansionStrategyTest {

    @Test
    public void testNullPatternGenerationSubset() {
        ConstantPatternExpansionStrategy out = new ConstantPatternExpansionStrategy(new int[][] {});
        Pattern nullPattern = out.generateNullPattern(createAttributes(5, new long[] {2, 4}));
        PatternEntry[] patternEntries = nullPattern.getPatternEntries();
        Assert.assertEquals(2, patternEntries.length);
        Assert.assertTrue(patternEntries[0] instanceof VariablePatternEntry);
        Assert.assertTrue(patternEntries[1] instanceof VariablePatternEntry);
    }

    @Test
    public void testNullPatternGenerationAllSet() {
        ConstantPatternExpansionStrategy out = new ConstantPatternExpansionStrategy(new int[][] {});
        Pattern nullPattern = out.generateNullPattern(createAttributes(7, new long[] {0, 1, 2, 3, 4, 5, 6}));
        PatternEntry[] patternEntries = nullPattern.getPatternEntries();
        Assert.assertEquals(7, patternEntries.length);
        Assert.assertTrue(patternEntries[0] instanceof VariablePatternEntry);
        Assert.assertTrue(patternEntries[1] instanceof VariablePatternEntry);
        Assert.assertTrue(patternEntries[2] instanceof VariablePatternEntry);
        Assert.assertTrue(patternEntries[3] instanceof VariablePatternEntry);
        Assert.assertTrue(patternEntries[4] instanceof VariablePatternEntry);
        Assert.assertTrue(patternEntries[5] instanceof VariablePatternEntry);
        Assert.assertTrue(patternEntries[6] instanceof VariablePatternEntry);
    }

    @Test
    public void testNullPatternGenerationNoneSet() {
        ConstantPatternExpansionStrategy out = new ConstantPatternExpansionStrategy(new int[][] {});
        Pattern nullPattern = out.generateNullPattern(createAttributes(3, new long[] {}));
        PatternEntry[] patternEntries = nullPattern.getPatternEntries();
        Assert.assertEquals(0, patternEntries.length);
    }

    @Test
    public void testGetChildPatternsNullPattern() {
        int[][] values = new int[][] {
            new int[] {0, 1},
            new int[] {2, 3}
        };
        ConstantPatternExpansionStrategy out = new ConstantPatternExpansionStrategy(values);
        Pattern nullPattern = out.generateNullPattern(createAttributes(2, new long[] {0, 1}));

        List<IntArrayList> cover = new ArrayList<>();
        cover.add(new IntArrayList(new int[] {0}));
        cover.add(new IntArrayList(new int[] {1}));
        nullPattern.setCover(cover);

        List<Pattern> children = out.getChildPatterns(nullPattern);

        Assert.assertEquals(4, children.size());
        Assert.assertTrue(containsPattern(children, new Pattern(createAttributeMap(new int[] {0, -1}))));
        Assert.assertTrue(containsPattern(children, new Pattern(createAttributeMap(new int[] {-1, 1}))));
        Assert.assertTrue(containsPattern(children, new Pattern(createAttributeMap(new int[] {2, -1}))));
        Assert.assertTrue(containsPattern(children, new Pattern(createAttributeMap(new int[] {-1, 3}))));
    }

    @Test
    public void testGetChildPatternsFromExistingPattern() {
        int[][] values = new int[][] {
                new int[] {0, 1, 2},
                new int[] {0, 3, 4}
        };
        Pattern pattern = new Pattern(createAttributeMap(new int[] {0, -1, -1}));

        List<IntArrayList> cover = new ArrayList<>();
        cover.add(new IntArrayList(new int[] {0}));
        cover.add(new IntArrayList(new int[] {1}));
        pattern.setCover(cover);

        ConstantPatternExpansionStrategy out = new ConstantPatternExpansionStrategy(values);
        List<Pattern> children = out.getChildPatterns(pattern);

        Assert.assertEquals(4, children.size());
        Assert.assertTrue(containsPattern(children, new Pattern(createAttributeMap(new int[] {0, 1, -1}))));
        Assert.assertTrue(containsPattern(children, new Pattern(createAttributeMap(new int[] {0, -1, 2}))));
        Assert.assertTrue(containsPattern(children, new Pattern(createAttributeMap(new int[] {0, 3, -1}))));
        Assert.assertTrue(containsPattern(children, new Pattern(createAttributeMap(new int[] {0, -1, 4}))));
    }

    @Test
    public void testGetChildPatternsFromExistingPatternWithNoVariables() {
        int[][] values = new int[][] {
                new int[] {0, 1, 2},
                new int[] {0, 1, 2}
        };
        Pattern pattern = new Pattern(createAttributeMap(new int[] {0, 1, 2}));

        List<IntArrayList> cover = new ArrayList<>();
        cover.add(new IntArrayList(new int[] {0}));
        cover.add(new IntArrayList(new int[] {1}));
        pattern.setCover(cover);

        ConstantPatternExpansionStrategy out = new ConstantPatternExpansionStrategy(values);
        List<Pattern> children = out.getChildPatterns(pattern);

        Assert.assertEquals(0, children.size());
    }

    private OpenBitSet createAttributes(long numBits, long[] setBits) {
        OpenBitSet attributes = new OpenBitSet(numBits);
        for (long setBit : setBits) {
            attributes.set(setBit);
        }
        return attributes;
    }

    private Map<Integer, PatternEntry> createAttributeMap(int[] values) {
        Map<Integer, PatternEntry> result = new HashMap<>(values.length);
        for (int i = 0; i < values.length; i += 1) {
            int value = values[i];
            if (value == -1) {
                result.put(i, new VariablePatternEntry());
            } else {
                result.put(i, new ConstantPatternEntry(value));
            }
        }
        return result;
    }

    private boolean containsPattern(Iterable<Pattern> patterns, Pattern pattern) {
        for (Pattern p : patterns) {
            if (p.equals(pattern)) {
                return true;
            }
        }
        return false;
    }
}
