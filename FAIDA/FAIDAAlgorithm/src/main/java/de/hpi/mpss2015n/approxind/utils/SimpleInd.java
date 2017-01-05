package de.hpi.mpss2015n.approxind.utils;

import com.google.common.collect.ComparisonChain;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

public final class SimpleInd implements Comparable<SimpleInd> {

    /**
     * Orders INDs (1) by their prefix, (2) their LHS suffix, and (3) finally by their RHS suffix.
     */
    public static Comparator<SimpleInd> prefixBlockComparator = new Comparator<SimpleInd>() {
        @Override
        public int compare(SimpleInd ind1, SimpleInd ind2) {
            // Sort by tables first.
            int diff = Integer.compare(ind1.left.getTable(), ind2.left.getTable());
            if (diff != 0) return diff;
            diff = Integer.compare(ind1.right.getTable(), ind2.right.getTable());
            if (diff != 0) return diff;

            // Sort alternatingly on the columns of the LHS and RHS.
            assert ind1.getArity() == ind2.getArity();
            int prefixLength = ind1.getArity() - 1;
            for (int i = 0; i < prefixLength; i++) {
                diff = Integer.compare(ind1.left.getColumn(i), ind2.left.getColumn(i));
                if (diff != 0) return diff;

                diff = Integer.compare(ind1.right.getColumn(i), ind2.right.getColumn(i));
                if (diff != 0) return diff;
            }
            diff = Integer.compare(ind1.left.getColumn(prefixLength), ind2.left.getColumn(prefixLength));
            if (diff != 0) return diff;

            return Integer.compare(ind1.right.getColumn(prefixLength), ind2.right.getColumn(prefixLength));
        }
    };

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

    public int getArity() {
        return this.left.getColumns().length;
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