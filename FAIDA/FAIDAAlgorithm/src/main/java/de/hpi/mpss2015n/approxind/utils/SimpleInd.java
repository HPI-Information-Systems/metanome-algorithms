package de.hpi.mpss2015n.approxind.utils;

import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;

public final class SimpleInd implements Comparable<SimpleInd> {
    public final SimpleColumnCombination left;
    public final SimpleColumnCombination right;

    public SimpleInd(SimpleColumnCombination left, SimpleColumnCombination right) {
        this.left = left;
        this.right = right;
    }

    public SimpleInd flipOff(int position) {
        return new SimpleInd(left.flipOff(position), right.flipOff(position));
    }

    public int size() {
        return left.getColumns().length;
    }

    public boolean startsWith(SimpleInd b) {
        return left.startsWith(b.left) && right.startsWith(b.right);
    }

    public SimpleInd combineWith(SimpleInd other) {
        SimpleColumnCombination newLeft = left.combineWith(other.left);
        SimpleColumnCombination newRight = right.combineWith(other.right);
        return new SimpleInd(newLeft, newRight);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleInd)) return false;
        SimpleInd other = (SimpleInd) o;
        return Objects.equals(left, other.left) && Objects.equals(right, other.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("left", left)
                .add("right", right).toString();
    }

    @Override
    public int compareTo(SimpleInd o) {
        return ComparisonChain.start().compare(left, o.left).compare(right, o.right).result();
    }


    /**
     * Create a builder for a new SimpleInd.
     * Usage: SimpleInd.lcolumns(table, columns...).right(table, columns)
     */
    public static SimpleIndBuilder left(int ltable, int... lcolumns) {
        return new SimpleIndBuilder(ltable, lcolumns);
    }

    public static class SimpleIndBuilder {
        private final int ltable;
        private final int[] lcolumns;

        private SimpleIndBuilder(int ltable, int[] lcolumns) {
            this.ltable = ltable;
            this.lcolumns = lcolumns;
        }

        public SimpleInd right(int rtable, int... rcolumns) {
            return new SimpleInd(
                    new SimpleColumnCombination(ltable, lcolumns),
                    new SimpleColumnCombination(rtable, rcolumns));
        }
    }


}