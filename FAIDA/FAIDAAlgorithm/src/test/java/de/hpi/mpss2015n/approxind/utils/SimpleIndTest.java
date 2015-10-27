package de.hpi.mpss2015n.approxind.utils;

import junit.framework.TestCase;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

public class SimpleIndTest extends TestCase {

    int TABLE = 0;

    SimpleInd a = SimpleInd.left(TABLE, 0, 1).right(TABLE, 0, 1);
    SimpleInd b1 = SimpleInd.left(TABLE, 1, 2).right(TABLE, 1, 2);
    SimpleInd b2 = SimpleInd.left(TABLE, 1, 2).right(TABLE, 1, 2);


    public void testEquals() throws Exception {
        assertThat(a.equals(a), is(true));
        assertThat(a.equals(b1), is(false));
        assertThat(b1.equals(b1), is(true));
        assertThat(b1.equals(b2), is(true));
    }

    public void testHashCode() throws Exception {
        assertThat(a.hashCode(), not(b1.hashCode()));
        assertThat(b1.hashCode(), is(b2.hashCode()));
    }

    public void testCompareTo() throws Exception {
        assertThat(a.compareTo(a), is(0));
        assertThat(a.compareTo(b1), not(0));
        assertThat(b1.compareTo(b1), is(0));
        assertThat(b1.compareTo(b2), is(0));
    }
}