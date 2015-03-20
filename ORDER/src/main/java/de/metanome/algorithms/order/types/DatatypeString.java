package de.metanome.algorithms.order.types;

import java.util.Comparator;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import de.metanome.algorithms.order.sorting.partitions.RowIndexedStringValue;

public class DatatypeString extends Datatype {
  public DatatypeString(final Comparator<String> comparator) {
    this.specificType = type.STRING;

    if (comparator == null) {
      this.indexedComparator = new Comparator<RowIndexedStringValue>() {

        @Override
        public int compare(final RowIndexedStringValue o1, final RowIndexedStringValue o2) {
          return ComparisonChain.start()
              .compare(o1.value, o2.value, Ordering.natural().nullsFirst()).result();
        }

      };
    } else {
      this.indexedComparator = new Comparator<RowIndexedStringValue>() {

        @Override
        public int compare(final RowIndexedStringValue o1, final RowIndexedStringValue o2) {
          return ComparisonChain.start()
              .compare(o1.value, o2.value, Ordering.from(comparator).nullsFirst()).result();
        }

      };
    }

  }
}
