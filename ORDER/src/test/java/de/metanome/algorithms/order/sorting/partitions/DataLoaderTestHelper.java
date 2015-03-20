package de.metanome.algorithms.order.sorting.partitions;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithms.order.types.Datatype;
import de.metanome.algorithms.order.types.TypeInferrer;


public class DataLoaderTestHelper {

  protected int[] columnIndices;
  protected List<List<RowIndexedValue>> dataByColumns;
  protected List<Datatype> types;
  protected RelationalInputGenerator inputGenerator;

  public DataLoaderTestHelper(final RelationalInputGenerator inputGenerator,
      final int[] columnIndices) {
    this.columnIndices = columnIndices;
    this.dataByColumns = new ArrayList<List<RowIndexedValue>>();
    this.inputGenerator = inputGenerator;

    for (int i = 0; i < columnIndices.length; i++) {
      this.dataByColumns.add(new ArrayList<RowIndexedValue>());
    }

    final TypeInferrer typeInferrer = new TypeInferrer(inputGenerator);
    try {
      this.types = typeInferrer.inferTypes();
    } catch (final InputGenerationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  protected void loadData() throws InputIterationException, InputGenerationException {
    this.dataByColumns = new ArrayList<List<RowIndexedValue>>();

    for (int i = 0; i < this.columnIndices.length; i++) {
      this.dataByColumns.add(new ArrayList<RowIndexedValue>());
    }

    try (final RelationalInput input = this.inputGenerator.generateNewCopy()) {

      long tupleId = 0;
      while (input.hasNext()) {
        final List<String> currentRow = input.next();
        for (final int column : this.columnIndices) {
          this.storeData(tupleId, currentRow, column);
        }
        tupleId++;
      }

    } catch (final Exception e) {
      throw new InputGenerationException("Error while loading data.", e);
    }

  }

  protected List<List<RowIndexedValue>> getData(final int[] columnIndicesSubset) {
    final List<List<RowIndexedValue>> dataForColumnSubset = new ArrayList<>();
    for (final int columnIndex : columnIndicesSubset) {
      dataForColumnSubset.add(this.dataByColumns.get(columnIndex));
    }
    return dataForColumnSubset;
  }

  private void storeData(final long tupleId, final List<String> currentRow, final int column)
      throws ParseException {
    switch (this.types.get(column).getSpecificType()) {
      case DOUBLE:
        this.dataByColumns.get(column).add(
            new RowIndexedDoubleValue(tupleId, Double.parseDouble(currentRow.get(column))));
        break;
      case DATE:
        this.dataByColumns.get(column).add(
            new RowIndexedDateValue(tupleId, DateUtils.parseDate(currentRow.get(column),
                TypeInferrer.dateFormats)));
        break;
      case LONG:
        this.dataByColumns.get(column).add(
            new RowIndexedLongValue(tupleId, Long.parseLong(currentRow.get(column))));
        break;
      case STRING:
        this.dataByColumns.get(column).add(
            new RowIndexedStringValue(tupleId, currentRow.get(column)));
        break;
      default:
        this.dataByColumns.get(column).add(
            new RowIndexedStringValue(tupleId, currentRow.get(column)));
        break;
    }
  }
}
