package de.metanome.algorithms.order.sorting.partitions;

import java.util.Date;

public class RowIndexedDateValue extends RowIndexedValue {
  public final Date value;

  public RowIndexedDateValue(final long index, final Date value) {
    this.index = index;
    this.value = value;
  }
}
