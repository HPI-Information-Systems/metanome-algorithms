package de.metanome.algorithms.order.types;

import java.util.Comparator;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import de.metanome.algorithms.order.sorting.partitions.RowIndexedLongValue;
import de.metanome.algorithms.order.sorting.partitions.RowIndexedValue;

/**
 *
 * Wrapper for the datatype Long. Expects a comparator of type {@link Long} to compare a
 * {@link RowIndexedValue}. If that comparator is <code>null</code>, defaults to natural ordering.
 * <code>null</code>s are always considered smaller than any other value.
 *
 * @author Philipp Langer
 *
 */
public class DatatypeLong extends Datatype {

  public DatatypeLong(final Comparator<Long> comparator) {
    this.specificType = type.LONG;

    if (comparator == null) {
      this.indexedComparator = new Comparator<RowIndexedLongValue>() {

        @Override
        public int compare(final RowIndexedLongValue o1, final RowIndexedLongValue o2) {
          return ComparisonChain.start()
              .compare(o1.value, o2.value, Ordering.natural().nullsFirst()).result();
        }

      };
    } else {
      this.indexedComparator = new Comparator<RowIndexedLongValue>() {

        @Override
        public int compare(final RowIndexedLongValue o1, final RowIndexedLongValue o2) {
          return ComparisonChain.start()
              .compare(o1.value, o2.value, Ordering.from(comparator).nullsFirst()).result();
        }

      };
    }

  }

}
