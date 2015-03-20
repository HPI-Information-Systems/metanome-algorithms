package de.metanome.algorithms.order.sorting.partitions;

public class RowIndexedLongValue extends RowIndexedValue {
  public final Long value;

  public RowIndexedLongValue(final long index, final Long value) {
    this.index = index;
    this.value = value;
  }
}
