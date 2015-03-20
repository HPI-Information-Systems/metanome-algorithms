package de.metanome.algorithms.order.sorting.partitions;

import it.unimi.dsi.fastutil.BigList;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashBigSet;
import it.unimi.dsi.fastutil.objects.ObjectBigArrayBigList;

import org.apache.lucene.util.OpenBitSet;

import de.metanome.algorithms.order.check.DependencyChecker;

public class SortedPartition {
  private final ObjectBigArrayBigList<LongOpenHashBigSet> orderedEquivalenceClasses;
  private boolean isUnique;
  private Long2LongOpenHashMap rowIndexToPosition;

  private final OpenBitSet equivalenceSetsBitRepresentation;
  private long currentBitSetIndex;
  private int numRows;

  // In the rowIndexToPosition hash map, indicates whether a row index is mapped
  // to the position of a equivalence class with more than one element.
  // default "not-found" return value = -1 is safe, because positions
  // in a list are always non-negative.
  public static final long POSITION_NOT_PRESENT = -1;

  /**
   * Creates a new sorted partition from the equivalence classes. For use in
   * {@link DependencyChecker#checkOrderDependencyForSwapStrictlySmaller(SortedPartition, SortedPartition)}
   *
   * @param equivalenceClasses
   * @return
   */
  public static SortedPartition copyFromEquivalenceClasses(final int numRows,
      final ObjectBigArrayBigList<LongOpenHashBigSet> equivalenceClasses) {
    final SortedPartition newSortedPartition = new SortedPartition(numRows);
    for (final LongOpenHashBigSet equivalenceClass : equivalenceClasses) {
      newSortedPartition.addEquivalenceClass(equivalenceClass.clone());
    }
    return newSortedPartition;
  }

  public SortedPartition(final int numRows) {
    this.orderedEquivalenceClasses = new ObjectBigArrayBigList<LongOpenHashBigSet>();
    this.isUnique = false;
    this.rowIndexToPosition = null;
    this.equivalenceSetsBitRepresentation = new OpenBitSet(numRows);
    this.numRows = numRows;
    this.currentBitSetIndex = 0;
  }

  public OpenBitSet getEquivalenceSetsBitRepresentation() {
    return this.equivalenceSetsBitRepresentation;
  }

  public void addEquivalenceClass(final LongOpenHashBigSet equivalenceClass) {
    this.orderedEquivalenceClasses.add(equivalenceClass);
    this.equivalenceSetsBitRepresentation.set(this.currentBitSetIndex);
    this.currentBitSetIndex += equivalenceClass.size64();
  }

  public void addEquivalenceClassByValues(final long... values) {
    final LongOpenHashBigSet equivalenceClass = new LongOpenHashBigSet();
    for (final long value : values) {
      equivalenceClass.add(value);
    }
    this.addEquivalenceClass(equivalenceClass);
  }

  public int getNumRows() {
    return this.numRows;
  }

  public void setNumRows(final int numRows) {
    this.numRows = numRows;
  }

  public void setUnique() {
    this.isUnique = true;
  }

  public boolean isUnique() {
    return this.isUnique;
  }

  /**
   * Multiply two sorted partitions A and B. The result is a sorted partition AB, which contains row
   * indices in the order of the tuples in AB.
   *
   * This method leverages the partial sorting of AB, knowing that A is already sorted.
   *
   * @param other
   * @return
   */
  public SortedPartition multiply(final SortedPartition other) {

    if (other == null) {
      throw new IllegalArgumentException("The provided sorted partition must not be null.");
    }

    this.buildHashMap();

    final Long2ObjectOpenHashMap<BigList<LongOpenHashBigSet>> rowIndexToEquivalenceClass =
        new Long2ObjectOpenHashMap<>();
    for (long i = 0; i < other.orderedEquivalenceClasses.size64(); i++) {
      final LongOpenHashBigSet otherEquivalenceClass = other.orderedEquivalenceClasses.get(i);
      final LongOpenHashBigSet touchedThisPositions =
          new LongOpenHashBigSet(otherEquivalenceClass.size64());
      for (final long rowIndex : otherEquivalenceClass) {
        final long positionInThis = this.rowIndexToPosition.get(rowIndex);

        if (positionInThis == POSITION_NOT_PRESENT) {
          continue;
        }

        touchedThisPositions.add(positionInThis);

        // lazy initialize the rowIndexToEquivalenceClass data structure
        if (rowIndexToEquivalenceClass.get(positionInThis) == null) {
          // this position has not been seen before,
          // equivalence class list not present yet => create new one
          rowIndexToEquivalenceClass.put(positionInThis,
              new ObjectBigArrayBigList<LongOpenHashBigSet>());
        }
        if (rowIndexToEquivalenceClass.get(positionInThis).size64() == 0) {
          // list of equivalence classes is empty => create first equivalence class
          final LongOpenHashBigSet newEquivalenceClass = new LongOpenHashBigSet();
          rowIndexToEquivalenceClass.get(positionInThis).add(newEquivalenceClass);
        }

        final long numberOfEquivalenceClasses =
            rowIndexToEquivalenceClass.get(positionInThis).size64();

        rowIndexToEquivalenceClass.get(positionInThis).get(numberOfEquivalenceClasses - 1)
        .add(rowIndex);

      }
      // current equivalence class of other is done. Create new entries for the next equivalence
      // class ...
      for (final long touchedPosition : touchedThisPositions) {
        rowIndexToEquivalenceClass.get(touchedPosition).add(new LongOpenHashBigSet());
      }
    }

    final SortedPartition multipliedSortedPartition = new SortedPartition(this.numRows);
    for (long thisPosition = 0; thisPosition < this.orderedEquivalenceClasses.size64(); thisPosition++) {
      final LongOpenHashBigSet equivalenceClass = this.orderedEquivalenceClasses.get(thisPosition);
      // if the equivalence class of A has only one element then, because of lexicographical
      // ordering, this equivalence class is carried over to AB
      if (equivalenceClass.size64() == 1) {
        multipliedSortedPartition.addEquivalenceClass(equivalenceClass);
        continue;
      }
      final BigList<LongOpenHashBigSet> equivalenceClassesAtThisPosition =
          rowIndexToEquivalenceClass.get(thisPosition);
      for (long i = 0; i < equivalenceClassesAtThisPosition.size64() - 1; i++) {
        // last one is an empty set by construction
        final LongOpenHashBigSet otherEquivalenceClass =
            rowIndexToEquivalenceClass.get(thisPosition).get(i);
        multipliedSortedPartition.addEquivalenceClass(otherEquivalenceClass);
      }
    }

    if (multipliedSortedPartition.size() == this.numRows) {
      multipliedSortedPartition.setUnique();
    }

    return multipliedSortedPartition;
  }

  public ObjectBigArrayBigList<LongOpenHashBigSet> getOrderedEquivalenceClasses() {
    return this.orderedEquivalenceClasses;
  }

  /**
   * build a hash map that maps row indices (a primitive long) to their position in the
   * {@link SortedPartition} (also a primitive long) if the row index is part of an equivalence
   * class with more than one element.
   *
   * @return
   */
  public void buildHashMap() {
    if (this.rowIndexToPosition != null) {
      return;
    }
    this.rowIndexToPosition = new Long2LongOpenHashMap();
    this.rowIndexToPosition.defaultReturnValue(POSITION_NOT_PRESENT);
    for (long i = 0; i < this.orderedEquivalenceClasses.size64(); i++) {
      final LongOpenHashBigSet equivalenceClass = this.orderedEquivalenceClasses.get(i);
      if (equivalenceClass.size64() == 1) {
        continue;
      }
      for (final long rowIndex : equivalenceClass) {
        this.rowIndexToPosition.put(rowIndex, i);
      }
    }
  }

  public LongOpenHashBigSet get(final long index) {
    return this.orderedEquivalenceClasses.get(index);
  }

  public long size() {
    return this.orderedEquivalenceClasses.size64();
  }

  @Override
  public String toString() {
    return this.orderedEquivalenceClasses.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime
            * result
            + ((this.orderedEquivalenceClasses == null) ? 0 : this.orderedEquivalenceClasses
                .hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final SortedPartition other = (SortedPartition) obj;
    if (this.orderedEquivalenceClasses == null) {
      if (other.orderedEquivalenceClasses != null) {
        return false;
      }
    } else if (!this.orderedEquivalenceClasses.equals(other.orderedEquivalenceClasses)) {
      return false;
    }
    return true;
  }
}
