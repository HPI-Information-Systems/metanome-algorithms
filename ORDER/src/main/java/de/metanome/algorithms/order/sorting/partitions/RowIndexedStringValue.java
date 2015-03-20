package de.metanome.algorithms.order.sorting.partitions;

public class RowIndexedStringValue extends RowIndexedValue {
  public final String value;

  public RowIndexedStringValue(final long index, final String value) {
    this.index = index;
    this.value = value;
  }
}
