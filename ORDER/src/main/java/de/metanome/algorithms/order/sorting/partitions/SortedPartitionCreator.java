package de.metanome.algorithms.order.sorting.partitions;

import it.unimi.dsi.fastutil.longs.LongOpenHashBigSet;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithms.order.types.Datatype;

/**
 * Create a partitioning into equivalence classes of the rows of the table underlying a
 * {@link RelationalInputGenerator} for a column of a given {@link Datatype}. The equivalence
 * classes are sorted into a list. The ordering of the partition is determined by a
 * {@link Comparator} or the natural order of the underlying datatype.
 *
 * @author Philipp Langer
 */
public class SortedPartitionCreator {

  public static SortedPartition createPartition(final List<RowIndexedValue> data,
      final Datatype type) {
    final Comparator<RowIndexedValue> indexedComparator = type.getIndexedComparator();

    if (indexedComparator == null) {
      throw new IllegalStateException("Comparator could not be created");
    }

    Collections.sort(data, indexedComparator);

    return createSortedPartition(data, indexedComparator);
  }

  private static SortedPartition createSortedPartition(final List<RowIndexedValue> data,
      final Comparator<RowIndexedValue> indexedComparator) {
    final SortedPartition sortedPartition = new SortedPartition(data.size());
    LongOpenHashBigSet currentEquivalenceClass = new LongOpenHashBigSet();
    RowIndexedValue previousValue = data.get(0);
    currentEquivalenceClass.add(previousValue.index);
    for (int i = 1; i < data.size(); i++) {
      final RowIndexedValue currentValue = data.get(i);
      if (indexedComparator.compare(previousValue, currentValue) == 0) {
        currentEquivalenceClass.add(currentValue.index);
        previousValue = currentValue;
      } else {
        sortedPartition.addEquivalenceClass(currentEquivalenceClass);
        currentEquivalenceClass = new LongOpenHashBigSet();
        currentEquivalenceClass.add(currentValue.index);
        previousValue = currentValue;
      }
    }
    sortedPartition.addEquivalenceClass(currentEquivalenceClass);

    if (sortedPartition.size() == data.size()) {
      // there are as many equivalence classes as there are rows
      sortedPartition.setUnique();
    }

    return sortedPartition;

  }

}
