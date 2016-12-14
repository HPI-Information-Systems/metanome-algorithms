package de.hpi.mpss2015n.approxind.utils;

import com.google.common.collect.ComparisonChain;
import de.metanome.algorithm_integration.ColumnCombination;

import java.util.Map;
import java.util.Objects;

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
        return combineWith(other, null);
    }

    public SimpleInd combineWith(SimpleInd other, Map<SimpleColumnCombination, SimpleColumnCombination> columnCombinations) {
        SimpleColumnCombination newLeft = left.combineWith(other.left, columnCombinations);
        SimpleColumnCombination newRight = right.combineWith(other.right, columnCombinations);
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
        return String.format("%s < %s", this.left, this.right);
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