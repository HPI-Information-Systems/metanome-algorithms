package de.metanome.algorithms.tireless.postprocessing;

import de.metanome.algorithms.tireless.regularexpression.containerclasses.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import static org.junit.Assert.*;

public class CombineSimilarCharClassesTest {

    @Test
    public void testCombineIdenticalClassesWithCount() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()) {{
                setMaxCount(2);
            }});
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()));
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()) {{
                setMinCount(0);
            }});
        }};

        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        combiner.mergeIdenticalClasses();

        assertEquals(1, conjunction.getLength());
        assertEquals(2, conjunction.getChild(0).getMinCount());
        assertEquals(4, conjunction.getChild(0).getMaxCount());
        assertEquals(new BitSet() {{
            set('a', 'c' + 1);
        }}, conjunction.getChild(0).getRepresentation());
    }

    @Test
    public void testDoNotMergeOtherExpressionTypes() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()));
            addChild(new RegularExpressionToken("abc", null, null));
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()));
            addChild(new RegularExpressionDisjunctionOfTokens(new HashMap<>() {{
                put("abc", 1);
            }}));
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()));
        }};


        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        combiner.mergeIdenticalClasses();

        assertEquals(5, conjunction.getLength());
        assertEquals(ExpressionType.CHARACTER_CLASS, conjunction.getChild(0).getExpressionType());
        assertEquals(ExpressionType.TOKEN, conjunction.getChild(1).getExpressionType());
        assertEquals(ExpressionType.CHARACTER_CLASS, conjunction.getChild(2).getExpressionType());
        assertEquals(ExpressionType.DISJUNCTION_OF_TOKENS, conjunction.getChild(3).getExpressionType());
        assertEquals(ExpressionType.CHARACTER_CLASS, conjunction.getChild(4).getExpressionType());
    }

    @Test
    public void testMergeOnlyDoubleTrues() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()));
            addChild(new RegularExpressionCharacterClass("def".toCharArray()));
            addChild(new RegularExpressionCharacterClass("ghi".toCharArray()) {{
                setMaxCount(5);
            }});
            addChild(new RegularExpressionCharacterClass("jkl".toCharArray()) {{
                setMinCount(0);
            }});
        }};


        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        ArrayList<Boolean> left = new ArrayList<>() {{
            add(false);
            add(true);
            add(true);
            add(false);
        }};
        ArrayList<Boolean> right = new ArrayList<>() {{
            add(false);
            add(false);
            add(true);
            add(false);
        }};
        combiner.merge(left, right, true, true);

        assertEquals(2, conjunction.getLength());
        assertEquals(new BitSet() {{
            set('a', 'c' + 1);
        }}, conjunction.getChild(0).getRepresentation());
        assertEquals(new BitSet() {{
            set('d', 'l' + 1);
        }}, conjunction.getChild(1).getRepresentation());
        assertEquals(7, conjunction.getChild(1).getMaxCount());
        assertEquals(2, conjunction.getChild(1).getMinCount());
        assertEquals(2, left.size());
        assertEquals(2, right.size());
        assertFalse(left.get(0));
        assertFalse(right.get(0));
        assertTrue(left.get(1));
        assertFalse(right.get(1));
    }

    @Test
    public void testDontMergeDoubleTrues() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()));
            addChild(new RegularExpressionCharacterClass("def".toCharArray()));
            addChild(new RegularExpressionCharacterClass("ghi".toCharArray()));
            addChild(new RegularExpressionCharacterClass("jkl".toCharArray()));
        }};


        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        ArrayList<Boolean> left = new ArrayList<>() {{
            add(false);
            add(true);
            add(false);
            add(true);
        }};
        ArrayList<Boolean> right = new ArrayList<>() {{
            add(true);
            add(false);
            add(true);
            add(false);
        }};
        combiner.merge(left, right, true, true);

        assertEquals(4, conjunction.getLength());
        assertEquals(4, left.size());
        assertEquals(4, right.size());
    }

    @Test
    public void testMergeOnlyLeft() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()));
            addChild(new RegularExpressionCharacterClass("def".toCharArray()) {{
                setMinCount(0);
            }});
            addChild(new RegularExpressionCharacterClass("ghi".toCharArray()) {{
                setMaxCount(4);
                setMinCount(0);
            }});
            addChild(new RegularExpressionCharacterClass("jkl".toCharArray()));
        }};


        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        ArrayList<Boolean> left = new ArrayList<>() {{
            add(false);
            add(true);
            add(true);
            add(false);
        }};
        ArrayList<Boolean> right = new ArrayList<>() {{
            add(false);
            add(false);
            add(false);
            add(false);
        }};
        combiner.merge(left, right, true, false);

        assertEquals(2, conjunction.getLength());
        assertEquals(new BitSet() {{
            set('a', 'i' + 1);
        }}, conjunction.getChild(0).getRepresentation());
        assertEquals(new BitSet() {{
            set('j', 'l' + 1);
        }}, conjunction.getChild(1).getRepresentation());
        assertEquals(6, conjunction.getChild(0).getMaxCount());
        assertEquals(1, conjunction.getChild(0).getMinCount());
        assertEquals(2, left.size());
        assertEquals(2, right.size());
        assertFalse(left.get(0));
        assertFalse(right.get(0));
        assertFalse(left.get(1));
        assertFalse(right.get(1));
    }

    @Test
    public void testDontMergeLeft() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()));
            addChild(new RegularExpressionCharacterClass("def".toCharArray()));
            addChild(new RegularExpressionCharacterClass("ghi".toCharArray()));
            addChild(new RegularExpressionCharacterClass("jkl".toCharArray()));
        }};


        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        ArrayList<Boolean> left = new ArrayList<>() {{
            add(false);
            add(false);
            add(true);
            add(false);
        }};
        ArrayList<Boolean> right = new ArrayList<>() {{
            add(false);
            add(false);
            add(false);
            add(false);
        }};
        combiner.merge(left, right, true, false);

        assertEquals(4, conjunction.getLength());
        assertEquals(4, left.size());
        assertEquals(4, right.size());
    }

    @Test
    public void testMergeOnlyLeftEdge() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()));
            addChild(new RegularExpressionCharacterClass("def".toCharArray()) {{
                setMinCount(0);
            }});
            addChild(new RegularExpressionCharacterClass("ghi".toCharArray()) {{
                setMaxCount(4);
                setMinCount(0);
            }});
        }};

        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        ArrayList<Boolean> left = new ArrayList<>() {{
            add(false);
            add(true);
            add(true);
        }};
        ArrayList<Boolean> right = new ArrayList<>() {{
            add(false);
            add(true);
            add(false);
        }};
        combiner.merge(left, right, true, false);

        assertEquals(1, conjunction.getLength());
        assertEquals(1, left.size());
        assertEquals(1, right.size());
    }

    @Test
    public void testMergeOnlyRight() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()));
            addChild(new RegularExpressionCharacterClass("def".toCharArray()) {{
                setMinCount(0);
            }});
            addChild(new RegularExpressionCharacterClass("ghi".toCharArray()) {{
                setMaxCount(4);
                setMinCount(0);
            }});
            addChild(new RegularExpressionCharacterClass("jkl".toCharArray()));
        }};


        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        ArrayList<Boolean> left = new ArrayList<>() {{
            add(false);
            add(false);
            add(false);
            add(false);
        }};
        ArrayList<Boolean> right = new ArrayList<>() {{
            add(false);
            add(true);
            add(true);
            add(false);
        }};
        combiner.merge(left, right, false, false);

        assertEquals(2, conjunction.getLength());
        assertEquals(new BitSet() {{
            set('a', 'c' + 1);
        }}, conjunction.getChild(0).getRepresentation());
        assertEquals(new BitSet() {{
            set('d', 'l' + 1);
        }}, conjunction.getChild(1).getRepresentation());
        assertEquals(6, conjunction.getChild(1).getMaxCount());
        assertEquals(1, conjunction.getChild(1).getMinCount());
        assertEquals(2, left.size());
        assertEquals(2, right.size());
        assertFalse(left.get(0));
        assertFalse(right.get(0));
        assertFalse(left.get(1));
        assertFalse(right.get(1));
    }

    @Test
    public void testDontMergeRight() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()));
            addChild(new RegularExpressionCharacterClass("def".toCharArray()));
            addChild(new RegularExpressionCharacterClass("ghi".toCharArray()));
            addChild(new RegularExpressionCharacterClass("jkl".toCharArray()));
        }};


        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        ArrayList<Boolean> left = new ArrayList<>() {{
            add(false);
            add(false);
            add(false);
            add(false);
        }};
        ArrayList<Boolean> right = new ArrayList<>() {{
            add(false);
            add(true);
            add(false);
            add(false);
        }};
        combiner.merge(left, right, true, false);

        assertEquals(4, conjunction.getLength());
        assertEquals(4, left.size());
        assertEquals(4, right.size());
    }

    @Test
    public void testMergeOnlyRightEdge() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()) {{
                setMinCount(0);
            }});
            addChild(new RegularExpressionCharacterClass("def".toCharArray()) {{
                setMaxCount(4);
                setMinCount(0);
            }});
            addChild(new RegularExpressionCharacterClass("ghi".toCharArray()));
        }};

        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        ArrayList<Boolean> left = new ArrayList<>() {{
            add(false);
            add(false);
            add(true);
        }};
        ArrayList<Boolean> right = new ArrayList<>() {{
            add(true);
            add(true);
            add(false);
        }};
        combiner.merge(left, right, false, false);

        assertEquals(1, conjunction.getLength());
        assertEquals(1, left.size());
        assertEquals(1, right.size());
    }
}
