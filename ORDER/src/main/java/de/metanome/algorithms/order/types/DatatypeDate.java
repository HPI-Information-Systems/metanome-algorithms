package de.metanome.algorithms.order.types;

import java.util.Comparator;
import java.util.Date;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import de.metanome.algorithms.order.sorting.partitions.RowIndexedDateValue;

public class DatatypeDate extends Datatype {

  private String dateFormat;

  public DatatypeDate(final Comparator<Date> comparator) {
    this(comparator, null);
  }

  public DatatypeDate(final Comparator<Date> comparator, final String dateFormat) {
    this.specificType = type.DATE;
    this.setDateFormat(dateFormat);

    if (comparator == null) {
      this.indexedComparator = new Comparator<RowIndexedDateValue>() {

        @Override
        public int compare(final RowIndexedDateValue o1, final RowIndexedDateValue o2) {
          return ComparisonChain.start()
              .compare(o1.value, o2.value, Ordering.natural().nullsFirst()).result();
        }

      };
    } else {
      this.indexedComparator = new Comparator<RowIndexedDateValue>() {

        @Override
        public int compare(final RowIndexedDateValue o1, final RowIndexedDateValue o2) {
          return ComparisonChain.start()
              .compare(o1.value, o2.value, Ordering.from(comparator).nullsFirst()).result();
        }

      };
    }
  }

  public String getDateFormat() {
    return dateFormat;
  }

  public void setDateFormat(String dateFormat) {
    this.dateFormat = dateFormat;
  }
}
