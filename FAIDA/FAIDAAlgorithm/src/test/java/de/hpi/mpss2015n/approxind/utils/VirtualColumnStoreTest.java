package de.hpi.mpss2015n.approxind.utils;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import de.hpi.mpss2015n.approxind.mocks.RelationalInputBuilder;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class VirtualColumnStoreTest {

    public static final int SAMPLE_SIZE = 10;
    RelationalInputBuilder rb = new RelationalInputBuilder("testTable");

    private static VirtualColumnStore createColumnStore(RelationalInputGenerator relationalInputGenerator) {
        RelationalInput relationalInput = null;
        try {
            relationalInput = relationalInputGenerator.generateNewCopy();
        } catch (InputGenerationException | AlgorithmConfigurationException e) {
            throw new RuntimeException("Could not access test data.", e);
        }
        VirtualColumnStore store = new VirtualColumnStore(relationalInput.numberOfColumns(), SAMPLE_SIZE, relationalInputGenerator, false);
        store.load("testDataset", 0, relationalInput);
        return store;
    }

    @Test
    public void testGetRows() throws Exception {
        rb.setHeader("col1", "col2", "col3")
                .addRow("a", "a", "b")
                .addRow("1", "2", "2");
        VirtualColumnStore cs = createColumnStore(rb.build());
        ColumnIterator rows = cs.getRows();

        long[] row = rows.next();
        assertThat(row[0], equalTo(row[1]));
        assertThat(row[0], not(equalTo(row[2])));

        row = rows.next();
        assertThat(row[0], not(equalTo(row[1])));
        assertThat(row[1], equalTo(row[2]));

        assertThat(rows.hasNext(), is(false));
    }

    @Test
    public void testGetRowsWithScc() throws Exception {
        rb.setHeader("col1", "col2", "col3").addRow("a", "a", "b");
        VirtualColumnStore cs = createColumnStore(rb.build());
        ColumnIterator rows = cs.getRows(SimpleColumnCombination.create(0, 0, 2));

        long[] row = rows.next();
        assertThat(row.length, is(2));

        assertThat(row[0], not(equalTo(row[1])));

        assertThat(rows.hasNext(), is(false));
    }

    @Test
    public void testHasNextTwice() throws Exception {
        rb.setHeader("col1", "col2").addRow("a", "a");
        VirtualColumnStore cs = createColumnStore(rb.build());
        ColumnIterator rows = cs.getRows();

        assertThat(rows.hasNext(), is(true));
        assertThat(rows.hasNext(), is(true));

        long[] row = rows.next();
        assertThat(row[0], equalTo(row[1]));

        assertThat(rows.hasNext(), is(false));
        assertThat(rows.hasNext(), is(false));
        assertThat(rows.hasNext(), is(false));
    }

    @Test
    public void testHashCache() throws Exception {
        rb.setHeader("col1").addRow("a").addRow("a");
        VirtualColumnStore cs = createColumnStore(rb.build());
        ColumnIterator rows = cs.getRows();

        assertThat(rows.hasNext(), is(true));
        long[] row1 = rows.next().clone();
        assertThat(rows.hasNext(), is(true));
        long[] row2 = rows.next().clone();
        assertThat(row1[0], equalTo(row2[0]));

        assertThat(rows.hasNext(), is(false));
    }

    @Test
    public void testGetLongRows() throws Exception {
        int count = 500_000;
        rb.setHeader("long_col");
        for (int i = 0; i < count; i++) {
            rb.addRow(Integer.toString(i));
        }
        VirtualColumnStore cs = createColumnStore(rb.build());
        ColumnIterator rows = cs.getRows();
        HashFunction hashFunction = Hashing.murmur3_128();
        int i = 0;
        while (rows.hasNext()) {
            long[] row = rows.next();
            long hash = hashFunction.hashString(Integer.toString(i), Charsets.UTF_8).asLong();
            assertThat("rowindex: " + i, row[0], equalTo(hash));
            i++;
        }
        assertThat(i, equalTo(count));
    }


//    @Test
//    public void testGetSample() throws Exception {
//        rb.setHeader("col1", "col2")
//                .addRow("a", "b").addRow("a", "b").addRow("a", "b").addRow("a", "b").addRow("a", "b")
//                .addRow("a", "b").addRow("a", "b").addRow("a", "b").addRow("a", "b").addRow("a", "b");
//        RelationalInput input = rb.build().generateNewCopy();
//        HashedColumnStore cs = new HashedColumnStore("sampleTest", 0, input, 5);
//        List<long[]> s1 = cs.getSample();
//        assertThat(s1.size(), equalTo(5));
//
//        cs = new HashedColumnStore("sampleTest", 0, input, 5);
//        List<long[]> s2 = cs.getSample();
//        assertThat(s2.size(), equalTo(5));
//
//        for (int i = 0; i < s1.size(); i++) {
//            assertArrayEquals(s2.get(i), s1.get(i));
//        }
//    }
}