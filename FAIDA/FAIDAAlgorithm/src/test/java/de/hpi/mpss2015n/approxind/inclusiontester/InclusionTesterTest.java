package de.hpi.mpss2015n.approxind.inclusiontester;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.common.hash.HashFunction;

import de.hpi.mpss2015n.approxind.InclusionTester;
import de.hpi.mpss2015n.approxind.utils.ColumnStore;
import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;

@RunWith(Parameterized.class)
public class InclusionTesterTest {

    public static final int TABLE0 = 0;

    private final HashFunction hashFunction = ColumnStore.HASH_FUNCTION;
    private final long aHash = hashFunction.hashUnencodedChars("a").asLong();
    private final long bHash = hashFunction.hashUnencodedChars("b").asLong();
    private final long cHash = hashFunction.hashUnencodedChars("c").asLong();
    private final long trueHash = hashFunction.hashUnencodedChars("true").asLong();
    private final long falseHash = hashFunction.hashUnencodedChars("false").asLong();
    private final long nullHash = hashFunction.hashUnencodedChars("").asLong();

    private InclusionTester t;

    // data() Parameters map to these constructor parameter
    public InclusionTesterTest(InclusionTester t) {
        this.t = t;
    }


    @Parameterized.Parameters(name = "{index}: inclusionTester: {0}")
    public static Iterable<Object> data() {
        return Arrays.asList(new Object[][]{
                {new HashSetInclusionTester()},
                {new HLLInclusionTester(0.01)},
                {new BloomFilterInclusionTester()}
        });
    }

    @Test
    public void testIsIncludedIn() throws Exception {
        SimpleColumnCombination c0 = SimpleColumnCombination.create(TABLE0, 0);
        SimpleColumnCombination c1 = SimpleColumnCombination.create(TABLE0, 1);
        SimpleColumnCombination c2 = SimpleColumnCombination.create(TABLE0, 2);

        t.setColumnCombinations(Arrays.asList(c0, c1, c2));

        t.startInsertRow(TABLE0);
        t.insertRow(new long[]{aHash, bHash, falseHash}, 0);
        t.insertRow(new long[]{bHash, bHash, trueHash}, 1);
        t.insertRow(new long[]{cHash, bHash, falseHash}, 2);

        t.finalizeInsertion();

        assertThat(t.isIncludedIn(c0, c0), is(true));
        assertThat(t.isIncludedIn(c0, c1), is(false));
        assertThat(t.isIncludedIn(c0, c2), is(false));
        assertThat(t.isIncludedIn(c1, c0), is(true));
        assertThat(t.isIncludedIn(c1, c1), is(true));
        assertThat(t.isIncludedIn(c1, c2), is(false));
        assertThat(t.isIncludedIn(c2, c0), is(false));
        assertThat(t.isIncludedIn(c2, c1), is(false));
        assertThat(t.isIncludedIn(c2, c2), is(true));
    }

    @Test
    public void testIsIncludedInSameColumns() throws Exception {
        SimpleColumnCombination c0 = SimpleColumnCombination.create(TABLE0, 0);
        SimpleColumnCombination c1 = SimpleColumnCombination.create(TABLE0, 1);

        t.setColumnCombinations(Arrays.asList(c0, c1));

        t.startInsertRow(TABLE0);
        t.insertRow(new long[]{aHash, aHash}, 0);
        t.insertRow(new long[]{bHash, bHash}, 1);

        t.finalizeInsertion();

        assertThat(t.isIncludedIn(c0, c0), is(true));
        assertThat(t.isIncludedIn(c0, c1), is(true));
        assertThat(t.isIncludedIn(c1, c0), is(true));
        assertThat(t.isIncludedIn(c1, c1), is(true));
    }

    @Test
    @Ignore
    public void testIsIncludedInWithNullValues() throws Exception {
        SimpleColumnCombination c0 = SimpleColumnCombination.create(TABLE0, 0);
        SimpleColumnCombination c1 = SimpleColumnCombination.create(TABLE0, 1);

        t.setColumnCombinations(Arrays.asList(c0, c1));

        t.startInsertRow(TABLE0);
        t.insertRow(new long[]{aHash, nullHash}, 0);
        t.insertRow(new long[]{bHash, nullHash}, 1);

        t.finalizeInsertion();

        assertThat(t.isIncludedIn(c0, c0), is(true));
        assertThat(t.isIncludedIn(c0, c1), is(false));
        assertThat(t.isIncludedIn(c1, c0), is(true));
        assertThat(t.isIncludedIn(c1, c1), is(true));
    }

    @Test
    public void testIsIncludedInMultiColumn() throws Exception {
        SimpleColumnCombination c01 = SimpleColumnCombination.create(TABLE0, 0, 1);
        SimpleColumnCombination c12 = SimpleColumnCombination.create(TABLE0, 1, 2);
        SimpleColumnCombination c23 = SimpleColumnCombination.create(TABLE0, 2, 3);

        t.setColumnCombinations(Arrays.asList(c01, c12, c23));

        t.startInsertRow(TABLE0);
        t.insertRow(new long[]{aHash, bHash, bHash, bHash}, 0);
        t.insertRow(new long[]{bHash, bHash, cHash, bHash}, 1);
        t.insertRow(new long[]{cHash, bHash, bHash, bHash}, 2);

        t.finalizeInsertion();

        assertThat(t.isIncludedIn(c01, c01), is(true));
        assertThat(t.isIncludedIn(c01, c12), is(false));
        assertThat(t.isIncludedIn(c01, c23), is(false));
        assertThat(t.isIncludedIn(c12, c01), is(false));
        assertThat(t.isIncludedIn(c12, c12), is(true));
        assertThat(t.isIncludedIn(c12, c23), is(false));
        assertThat(t.isIncludedIn(c23, c01), is(true));
        assertThat(t.isIncludedIn(c23, c12), is(false));
        assertThat(t.isIncludedIn(c23, c23), is(true));

    }

    @Test
    public void testIsIncludedInMultiTables() throws Exception {
        SimpleColumnCombination c0 = SimpleColumnCombination.create(TABLE0, 0);
        SimpleColumnCombination c1 = SimpleColumnCombination.create(TABLE0 + 2, 0);

        t.setColumnCombinations(Arrays.asList(c0, c1));

        t.startInsertRow(TABLE0);
        t.insertRow(new long[]{aHash}, 0);
        t.insertRow(new long[]{bHash}, 1);
        t.startInsertRow(TABLE0 + 2);
        t.insertRow(new long[]{bHash}, 2);
        t.insertRow(new long[]{bHash}, 3);

        t.finalizeInsertion();

        assertThat(t.isIncludedIn(c0, c1), is(false));
        assertThat(t.isIncludedIn(c1, c0), is(true));
    }
}