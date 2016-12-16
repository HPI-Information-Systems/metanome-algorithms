package de.hpi.mpss2015n.approxind.utils;

import com.google.common.base.Verify;
import com.google.common.primitives.Ints;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

public final class SimpleColumnCombination implements Comparable<SimpleColumnCombination> {
    private static final Comparator<int[]> COMPARATOR = Ints.lexicographicalComparator();

    private final int table;
    private final long distinctCount;
    private final int[] columns;
    private boolean active;
    private int index;


    public SimpleColumnCombination(int table, int[] columns) {
        this.table = table;
        this.columns = columns;
        this.distinctCount = 0;
        active = true;
    }

    private SimpleColumnCombination(int table, int[] columns, int additionalColumn) {
        this.table = table;
        this.columns = Arrays.copyOf(columns, columns.length + 1);
        this.columns[columns.length] = additionalColumn;
        this.distinctCount = 0;
        active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getDistinctCount() {
        return distinctCount;
    }

    public static SimpleColumnCombination create(int table, int... columns) {
        return new SimpleColumnCombination(table, columns);
    }

    public SimpleColumnCombination flipOff(int position) {
        int[] newColumns = new int[columns.length - 1];
        System.arraycopy(columns, 0, newColumns, 0, position);
        System.arraycopy(columns, position + 1, newColumns, position, columns.length - position - 1);
        return new SimpleColumnCombination(table, newColumns);
    }

    public int getTable() {
        return table;
    }

    public int getColumn(int index) {
        return this.columns[index];
    }

    public int[] getColumns() {
        return columns;
    }

    public boolean startsWith(SimpleColumnCombination other) {
        if (table != other.table) {
            return false;
        }
        Verify.verify(columns.length == other.columns.length);
        for (int i = 0; i < columns.length - 1; i++) {
            if (columns[i] != other.columns[i]) {
                return false;
            }
        }
        return true;
    }

    public SimpleColumnCombination combineWith(SimpleColumnCombination other, Map<SimpleColumnCombination, SimpleColumnCombination> columnCombinations) {
        Verify.verify(table == other.table, "only merge inside a table");
        SimpleColumnCombination combinedCombination = new SimpleColumnCombination(table, columns, other.lastColumn());
        if (columnCombinations != null) {
            SimpleColumnCombination existingCombination = columnCombinations.get(combinedCombination);
            if (existingCombination == null) {
                columnCombinations.put(combinedCombination, combinedCombination);
            } else {
                combinedCombination = existingCombination;
            }
        }
        return combinedCombination;
    }

    public int lastColumn() {
        return columns[columns.length - 1];
    }

    @Override
    public int hashCode() {
        return Objects.hash(table) ^ Arrays.hashCode(columns);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SimpleColumnCombination)) return false;
        SimpleColumnCombination other = (SimpleColumnCombination) obj;
        return table == other.table && Arrays.equals(columns, other.columns);
    }

    @Override
    public int compareTo(SimpleColumnCombination o) {
        if (table != o.table)
            return Integer.compare(table, o.table);
        return COMPARATOR.compare(columns, o.columns);
    }

    @Override
    public String toString() {
        return String.valueOf(this.table) + "." + Arrays.toString(this.columns);
    }

    public int setIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
