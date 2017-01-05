package de.hpi.mpss2015n.approxind.utils;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import org.junit.Test;


public class SimpleColumnCombinationTest {

    public static final int TABLE = 0;

    @Test
    public void testGetTable() throws Exception {
        SimpleColumnCombination a = SimpleColumnCombination.create(TABLE, 2,3,4);
        assertThat(a.getTable(), equalTo(TABLE));
    }

    @Test
    public void testGetColumns() throws Exception {
        SimpleColumnCombination a = SimpleColumnCombination.create(TABLE, 2,3,4);
        assertThat(a.getColumns(), equalTo(new int[]{2, 3, 4}));
    }

    @Test
    public void testStartsWith() throws Exception {
        SimpleColumnCombination a = SimpleColumnCombination.create(TABLE, 2,3,5);
        SimpleColumnCombination b = SimpleColumnCombination.create(TABLE+1, 2,3,5);
        SimpleColumnCombination c = SimpleColumnCombination.create(TABLE, 2,3,7);
        SimpleColumnCombination d = SimpleColumnCombination.create(TABLE, 1,3,5);

        assertThat(a.startsWith(a), is(true));
        assertThat(a.startsWith(b), is(false));
        assertThat(a.startsWith(c), is(true));
        assertThat(a.startsWith(d), is(false));

        assertThat(b.startsWith(a), is(false));
        assertThat(b.startsWith(b), is(true));
        assertThat(b.startsWith(c), is(false));
        assertThat(b.startsWith(d), is(false));

        assertThat(c.startsWith(a), is(true));
        assertThat(c.startsWith(b), is(false));
        assertThat(c.startsWith(c), is(true));
        assertThat(c.startsWith(d), is(false));
    }

    @Test
    public void testCombineWith() throws Exception {
        SimpleColumnCombination a = SimpleColumnCombination.create(TABLE, 2,3,6);
        SimpleColumnCombination b = SimpleColumnCombination.create(TABLE, 2,3,7);

        SimpleColumnCombination result = SimpleColumnCombination.create(TABLE, 2, 3, 6, 7);
        assertThat(a.combineWith(b, null), equalTo(result));
    }

    @Test
    public void testFlipOff() throws Exception {
        SimpleColumnCombination a = SimpleColumnCombination.create(TABLE, 1,2,3);
        SimpleColumnCombination b = a.flipOff(0);
        SimpleColumnCombination c = a.flipOff(1);
        SimpleColumnCombination d = a.flipOff(2);

        assertThat(b, equalTo(SimpleColumnCombination.create(TABLE, 2, 3)));
        assertThat(c, equalTo(SimpleColumnCombination.create(TABLE, 1, 3)));
        assertThat(d, equalTo(SimpleColumnCombination.create(TABLE, 1, 2)));
    }
}