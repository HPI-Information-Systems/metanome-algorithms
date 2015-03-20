package de.metanome.algorithms.order.sorting.partitions;

import static org.junit.Assert.assertEquals;
import it.unimi.dsi.fastutil.longs.LongOpenHashBigSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ComparisonChain;

import de.metanome.algorithms.order.types.Datatype;
import de.metanome.algorithms.order.types.DatatypeDate;
import de.metanome.algorithms.order.types.DatatypeLong;


public class SortedPartitionCreatorTest {

  protected TestCsvFileFixture csvFileFixture;

  protected File testFile;
  protected TestFileInputGenerator generator;
  protected List<List<RowIndexedValue>> data;
  protected List<List<RowIndexedValue>> dataOfTypeDate;
  protected int[] columnIndices;
  protected int[] columnIndicesLong;
  protected int[] columnIndicesDate;
  protected int numRows;
  protected DataLoaderTestHelper dataLoader;

  @Before
  public void setUp() throws Exception {
    this.csvFileFixture = new TestCsvFileFixture();
    this.testFile = this.csvFileFixture.getTestDataPath("test.csv");
    this.generator = new TestFileInputGenerator(this.testFile);
    this.columnIndices = new int[] {0, 1, 2, 3, 4, 5};

    this.dataLoader = new DataLoaderTestHelper(this.generator, this.columnIndices);
    this.dataLoader.loadData();

    this.data = this.dataLoader.getData(this.columnIndices);

    this.columnIndicesLong = new int[3];
    this.columnIndicesLong[0] = 0;
    this.columnIndicesLong[1] = 1;
    this.columnIndicesLong[2] = 2;

    this.columnIndicesDate = new int[3];
    this.columnIndicesDate[0] = 3;
    this.columnIndicesDate[1] = 4;
    this.columnIndicesDate[2] = 5;

    this.numRows = this.data.get(0).size();
  }

  @Test
  public void testCreatePartitionFromLongtypeNatural() {
    // Setup
    final Comparator<Long> longComparator = null; // use natural ordering of Long
    final Datatype type = new DatatypeLong(longComparator);
    // Expected values
    final ArrayList<SortedPartition> expectedSortedPartitions = new ArrayList<>();

    final SortedPartition firstSortedPartition = new SortedPartition(this.numRows);
    this.addEquivalenceClass(firstSortedPartition, 0L);
    this.addEquivalenceClass(firstSortedPartition, 1L, 2L);
    expectedSortedPartitions.add(firstSortedPartition);

    final SortedPartition secondSortedPartition = new SortedPartition(this.numRows);
    this.addEquivalenceClass(secondSortedPartition, 1L);
    this.addEquivalenceClass(secondSortedPartition, 0L, 2L);
    expectedSortedPartitions.add(secondSortedPartition);

    final SortedPartition thirdSortedPartition = new SortedPartition(this.numRows);
    this.addEquivalenceClass(thirdSortedPartition, 0L, 1L);
    this.addEquivalenceClass(thirdSortedPartition, 2L);
    expectedSortedPartitions.add(thirdSortedPartition);
    // Execute functionality
    // Check result
    for (int columnIndex = 0; columnIndex < this.columnIndicesLong.length; columnIndex++) {
      final SortedPartition sortedPartition =
          SortedPartitionCreator.createPartition(
              this.data.get(this.columnIndicesLong[columnIndex]), type);
      // by construction, expectedSortedPartitions contains the columns' partitions in order
      // of the column indices
      assertEquals(sortedPartition,
          expectedSortedPartitions.get(this.columnIndicesLong[columnIndex]));
    }

  }

  @Test
  public void testCreatePartitionFromLongtypeCustomComparator() {
    // Setup
    final Comparator<Long> longComparator = new Comparator<Long>() {
      @Override
      public int compare(final Long first, final Long second) {
        // inverse natural ordering
        return ComparisonChain.start().compare(first, second).result() * (-1);
      }
    };
    final Datatype type = new DatatypeLong(longComparator);
    // Expected values
    final ArrayList<SortedPartition> expectedSortedPartitions = new ArrayList<>();
    // reverse natural ordering: expect same equivalence classes in reverse order
    final SortedPartition firstSortedPartition = new SortedPartition(this.numRows);
    this.addEquivalenceClass(firstSortedPartition, 1L, 2L);
    this.addEquivalenceClass(firstSortedPartition, 0L);
    expectedSortedPartitions.add(firstSortedPartition);

    final SortedPartition secondSortedPartition = new SortedPartition(this.numRows);
    this.addEquivalenceClass(secondSortedPartition, 0L, 2L);
    this.addEquivalenceClass(secondSortedPartition, 1L);
    expectedSortedPartitions.add(secondSortedPartition);

    final SortedPartition thirdSortedPartition = new SortedPartition(this.numRows);
    this.addEquivalenceClass(thirdSortedPartition, 2L);
    this.addEquivalenceClass(thirdSortedPartition, 0L, 1L);
    expectedSortedPartitions.add(thirdSortedPartition);
    // Execute functionality
    // Check result
    for (int i = 0; i < this.columnIndicesLong.length; i++) {
      final SortedPartition sortedPartition =
          SortedPartitionCreator.createPartition(this.data.get(this.columnIndicesLong[i]), type);
      assertEquals(sortedPartition, expectedSortedPartitions.get(i));
    }

  }

  @Test
  public void testCreatePartitionGenericDate() {
    // Setup
    final Datatype type = new DatatypeDate(null);
    // Expected values
    final ArrayList<SortedPartition> expectedSortedPartitions = new ArrayList<>();
    final SortedPartition firstSortedPartition = new SortedPartition(this.numRows);
    this.addEquivalenceClass(firstSortedPartition, 0L);
    this.addEquivalenceClass(firstSortedPartition, 1L, 2L);
    expectedSortedPartitions.add(firstSortedPartition);

    final SortedPartition secondSortedPartition = new SortedPartition(this.numRows);
    this.addEquivalenceClass(secondSortedPartition, 1L);
    this.addEquivalenceClass(secondSortedPartition, 0L, 2L);
    expectedSortedPartitions.add(secondSortedPartition);

    final SortedPartition thirdSortedPartition = new SortedPartition(this.numRows);
    this.addEquivalenceClass(thirdSortedPartition, 0L, 1L);
    this.addEquivalenceClass(thirdSortedPartition, 2L);
    expectedSortedPartitions.add(thirdSortedPartition);
    // Execute functionality
    // Check result
    for (int i = 0; i < this.columnIndicesDate.length; i++) {
      final SortedPartition sortedPartition =
          SortedPartitionCreator.createPartition(this.data.get(this.columnIndicesDate[i]), type);
      assertEquals(expectedSortedPartitions.get(i), sortedPartition);

    }

  }

  private void addEquivalenceClass(final SortedPartition firstSortedPartition, final long... longs) {
    final LongOpenHashBigSet equivalenceClass = new LongOpenHashBigSet();
    for (final long value : longs) {
      equivalenceClass.add(value);
    }
    firstSortedPartition.addEquivalenceClass(equivalenceClass);
  }
}
