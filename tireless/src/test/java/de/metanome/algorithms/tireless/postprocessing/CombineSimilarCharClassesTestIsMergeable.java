package de.metanome.algorithms.tireless.postprocessing;

import de.metanome.algorithms.tireless.regularexpression.containerclasses.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CombineSimilarCharClassesTestIsMergeable {

    @Test
    public void testIsMergeableTransitiveLeft() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abcd".toCharArray()));
            addChild(new RegularExpressionCharacterClass("abcdefg".toCharArray()) {{
                setMinCount(0);
            }});
            addChild(new RegularExpressionCharacterClass("abcdefghijklm".toCharArray()) {{
                setMinCount(0);
            }});
        }};

        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        List<Boolean> result = combiner.isMergeable(true);
        List<Boolean> expected = List.of(false, true, true);

        assertEquals(expected, result);
    }

    @Test
    public void testIsMergeableTransitiveRight() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abcdefghijklm".toCharArray()) {{
                setMinCount(0);
            }});
            addChild(new RegularExpressionCharacterClass("abcdefg".toCharArray()) {{
                setMinCount(0);
            }});
            addChild(new RegularExpressionCharacterClass("abcd".toCharArray()));
        }};

        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        List<Boolean> result = combiner.isMergeable(false);
        List<Boolean> expected = List.of(true, true, false);

        assertEquals(expected, result);
    }

    @Test
    public void testIsMergeableOtherTypeLeft() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abcd".toCharArray()));
            addChild(new RegularExpressionToken("abcdefg", null, null) {{
                setMinCount(0);
            }});
        }};

        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        List<Boolean> result = combiner.isMergeable(true);
        List<Boolean> expected = List.of(false, true);

        assertEquals(expected, result);
    }

    @Test
    public void testIsMergeableOtherTypeRight() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionToken("abcdefg", null, null) {{
                setMinCount(0);
            }});
            addChild(new RegularExpressionCharacterClass("abcd".toCharArray()));
        }};

        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        List<Boolean> result = combiner.isMergeable(false);
        List<Boolean> expected = List.of(true, false);

        assertEquals(expected, result);
    }

    @Test
    public void testIsMergeableWithGapLeft() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abcd".toCharArray()));
            addChild(new RegularExpressionToken("c", null, null) {{
                setMinCount(0);
            }});
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()) {{
                setMinCount(0);
            }});
        }};

        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        List<Boolean> result = combiner.isMergeable(true);
        List<Boolean> expected = List.of(false, true, true);

        assertEquals(expected, result);
    }

    @Test
    public void testIsMergeableWithGapRight() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abc".toCharArray()) {{
                setMinCount(0);
            }});
            addChild(new RegularExpressionToken("c", null, null) {{
                setMinCount(0);
            }});
            addChild(new RegularExpressionCharacterClass("abcd".toCharArray()));
        }};

        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        List<Boolean> result = combiner.isMergeable(false);
        List<Boolean> expected = List.of(true, true, false);

        assertEquals(expected, result);
    }

    @Test
    public void testIsMergeableNoMergeLeft() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abcd".toCharArray()));
            addChild(new RegularExpressionToken("c", null, null) {{
                setMinCount(0);
            }});
            addChild(new RegularExpressionCharacterClass("abcdefgh".toCharArray()) {{
                setMinCount(0);
            }});
        }};

        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        List<Boolean> result = combiner.isMergeable(true);
        List<Boolean> expected = List.of(false, true, false);

        assertEquals(expected, result);
    }

    @Test
    public void testIsMergeableNoMergeRight() {
        RegularExpressionConjunction conjunction = new RegularExpressionConjunction() {{
            addChild(new RegularExpressionCharacterClass("abcdefgh".toCharArray()) {{
                setMinCount(0);
            }});
            addChild(new RegularExpressionToken("c", null, null) {{
                setMinCount(0);
            }});
            addChild(new RegularExpressionCharacterClass("abcd".toCharArray()));
        }};

        CombineSimilarCharClasses combiner = new CombineSimilarCharClasses(conjunction);
        List<Boolean> result = combiner.isMergeable(false);
        List<Boolean> expected = List.of(false, true, false);

        assertEquals(expected, result);
    }
}
